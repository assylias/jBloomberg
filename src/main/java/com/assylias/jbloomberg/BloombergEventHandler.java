/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.CorrelationID;
import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Event;
import com.bloomberglp.blpapi.EventHandler;
import com.bloomberglp.blpapi.Message;
import com.bloomberglp.blpapi.Name;
import com.bloomberglp.blpapi.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static com.assylias.jbloomberg.BloombergEventHandler.BloombergConnectionState.SESSION_STARTED;
import static com.assylias.jbloomberg.BloombergEventHandler.BloombergConnectionState.SESSION_STARTUP_FAILURE;
import static java.util.Objects.requireNonNull;

/**
 * An implementation of EventHandler. This is where all the messages are received from the Bloomberg session and
 * forwarded to the relevant parsers for requests or to a queue for subscriptions.
 * The typical lifecycle is as follows:
 * <ul>
 * <li> SessionConnectionUp </li>
 * <li> SessionStarted </li>
 * <li> SessionConnectionDown </li>
 * <li> SessionTerminated </li>
 * </ul>
 * A SessionConnectionDown will be sent if the terminal gets logged out by another device - if setAutoRestartOnDisconnection has been set to true, it will be
 * followed by a SessionConnectionUp although no data will be coming until the user logs in again. If setAutoRestartOnDisconnection has been set to false (default)
 * it will be followed by a SessionTerminated signal.
 */
final class BloombergEventHandler implements EventHandler {

    private final static Logger logger = LoggerFactory.getLogger(BloombergEventHandler.class);
    private final BlockingQueue<DataOrSubscriptionError> subscriptionDataQueue;
    private final Consumer<SessionState> stateListener;
    private final Map<CorrelationID, ResultParser<?>> parsers = new ConcurrentHashMap<>();
    private volatile Runnable runOnSessionStarted;
    private volatile Consumer<BloombergException> runOnSessionStartupFailure;

    /**
     *
     * @param subscriptionDataQueue the queue to which subscription data will be posted.
     * @param stateListener a listener that will be called on each new SESSION_STATUS event.
     *
     * @throws NullPointerException if any of the arguments are null.
     */
    public BloombergEventHandler(BlockingQueue<DataOrSubscriptionError> subscriptionDataQueue, Consumer<SessionState> stateListener) {
        this.subscriptionDataQueue = requireNonNull(subscriptionDataQueue);
        this.stateListener = requireNonNull(stateListener);
    }

    @Override
    public void processEvent(Event event, Session session) {
        try {
            EventTypeEnum type = EventTypeEnum.get(event);
            switch (type) {
                case SESSION_STATUS:
                    for (Message msg : event) {
                        logger.debug("[{}] {}", type, msg);
                        BloombergConnectionState state = BloombergConnectionState.get(msg.messageType());
                        if (state == SESSION_STARTED) runOnSessionStarted.run();
                        if (state == SESSION_STARTUP_FAILURE) runOnSessionStartupFailure.accept(new BloombergException(msg.toString()));
                        if (state != null) stateListener.accept(SessionState.from(state));
                    }
                    break;
                case PARTIAL_RESPONSE:
                    for (Message msg : event) {
                        logger.trace("[{}] {}", type, msg);
                        CorrelationID cId = msg.correlationID();
                        ResultParser<?> parser = parsers.get(cId);
                        if (parser != null) {
                            parser.addMessage(msg);
                        }
                    }
                    break;
                case RESPONSE:
                case TOKEN_STATUS:
                case AUTHORIZATION_STATUS:
                    Set<CorrelationID> endOfTransmission = new HashSet<>();
                    for (Message msg : event) {
                        logger.trace("[{}] {}", type, msg);
                        CorrelationID cId = msg.correlationID();
                        ResultParser<?> parser = parsers.get(cId);
                        if (parser != null) {
                            endOfTransmission.add(cId);
                            parser.addMessage(msg);
                        }
                    }
                    for (CorrelationID cId : endOfTransmission) {
                        ResultParser<?> parser = parsers.remove(cId); //remove from the map - not needed any longer.
                        parser.noMoreMessages();
                    }
                    break;
                case SUBSCRIPTION_DATA:
                    for (Message msg : event) {
                        CorrelationID id = msg.correlationID();
                        int numFields = msg.asElement().numElements();
                        Map<String, Object> data = new LinkedHashMap<>();
                        for (int i = 0; i < numFields; ++i) {
                            Element field = msg.asElement().getElement(i);
                            if (!field.isNull()) {
                                data.put(field.name().toString(), BloombergUtils.getSpecificObjectOf(field));
                            }
                        }
                        try {
                            subscriptionDataQueue.put(DataOrSubscriptionError.of(id, data));
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return; //ignore the rest
                        }
                        logger.trace("[SUBS_DATA] {}", data);
                    }
                    break;
                case SUBSCRIPTION_STATUS:
                    for (Message msg : event) {
                        CorrelationID id = msg.correlationID();
                        logger.debug("[{}] id=[{}] {}", type, id, msg);

                        String msgType = msg.messageType().toString();
                        if (msgType == null || !msgType.startsWith("SubscriptionStarted")) {
                            logger.debug("[{}] id=[{}] {}", type, id, msg);
                            Element msgElement = msg.asElement();
                            DataOrSubscriptionError data = null;
                            if (msgElement.hasElement("reason")){
                                Element reason = msg.asElement().getElement("reason");
                                if (reason.hasElement("errorCode") && reason.hasElement("category") && reason.hasElement("description")) {
                                    SubscriptionError e = new SubscriptionError(msgType, msg.topicName(), reason.getElementAsInt32("errorCode"),
                                            reason.getElementAsString("category"), reason.getElementAsString("description"));
                                    data = DataOrSubscriptionError.of(id, e);
                                }
                            }
                            if (data == null) data = DataOrSubscriptionError.of(id, new SubscriptionError(msgType, msg.topicName(), 0, "", msg.toString()));
                            try {
                                subscriptionDataQueue.put(data);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return; //ignore the rest
                            }
                        }
                    }
                    break;
                default:
                    for (Message msg : event) {
                        CorrelationID id = msg.correlationID();
                        logger.debug("[{}] id=[{}] {}", type, id, msg);
                    }
            }
            //catch all - this code will run in one of the Bloomberg API's threads so we have no way to catch exceptions otherwise
        } catch (Exception e) {
            logger.error("Error in Bloomberg EventHandler: " + e.getMessage(), e);
            throw e; //rethrow in case the Bloomberg API needs to see it
        }
    }

    /**
     *
     * @param runOnSessionStarted this runnable will be run as soon as the session is started
     */
    void onSessionStarted(Runnable runOnSessionStarted) {
        this.runOnSessionStarted = runOnSessionStarted;
    }

    /**
     *
     * @param runOnSessionStartupFailure a runnable to run if the session startup process fails
     */
    void onSessionStartupFailure(Consumer<BloombergException> runOnSessionStartupFailure) {
        this.runOnSessionStartupFailure = runOnSessionStartupFailure;
    }

    void setParser(CorrelationID requestId, ResultParser<?> parser) {
        parsers.put(requestId, parser);
    }

    /**
     * Wrapping all the EventType objects in an enum for easier use
     */
    static enum EventTypeEnum {

        ADMIN(Event.EventType.ADMIN),
        AUTHORIZATION_STATUS(Event.EventType.AUTHORIZATION_STATUS),
        PARTIAL_RESPONSE(Event.EventType.PARTIAL_RESPONSE),
        REQUEST(Event.EventType.REQUEST),
        REQUEST_STATUS(Event.EventType.REQUEST_STATUS),
        RESOLUTION_STATUS(Event.EventType.RESOLUTION_STATUS),
        RESPONSE(Event.EventType.RESPONSE),
        SERVICE_STATUS(Event.EventType.SERVICE_STATUS),
        SESSION_STATUS(Event.EventType.SESSION_STATUS),
        SUBSCRIPTION_DATA(Event.EventType.SUBSCRIPTION_DATA),
        SUBSCRIPTION_STATUS(Event.EventType.SUBSCRIPTION_STATUS),
        TIMEOUT(Event.EventType.TIMEOUT),
        TOKEN_STATUS(Event.EventType.TOKEN_STATUS),
        TOPIC_STATUS(Event.EventType.TOPIC_STATUS);
        private final static Map<Event.EventType, EventTypeEnum> map = new HashMap<>(EventTypeEnum.values().length, 1);

        static {
            for (EventTypeEnum e : values()) {
                map.put(e.evtType, e);
            }
        }
        private final Event.EventType evtType;

        private EventTypeEnum(Event.EventType evtType) {
            this.evtType = evtType;
        }

        static EventTypeEnum get(Event evt) {
            return map.get(evt.eventType());
        }
    }

    /**
     * BloombergConnectionState is an enum representing the possible states of the underlying Bloomberg connection. The difference with the SessionState enum
     * is that it only contains states sent by the Bloomberg connection.
     */
    static enum BloombergConnectionState {
        SESSION_STARTED("SessionStarted"),
        SESSION_STARTUP_FAILURE("SessionStartupFailure"),
        SESSION_CONNECTION_DOWN("SessionConnectionDown"),
        SESSION_CONNECTION_UP("SessionConnectionUp"),
        SESSION_TERMINATED("SessionTerminated");

        private final static Map<Name, BloombergConnectionState> map = new HashMap<>(BloombergConnectionState.values().length, 1);

        static {
            for (BloombergConnectionState e : values()) map.put(e.name, e);
        }
        private final Name name;

        private BloombergConnectionState(String s) {
            this.name = new Name(s);
        }
        static BloombergConnectionState get(Name name) {
            BloombergConnectionState s = map.get(name);
            if (s == null) logger.info("Not a valid connection state: " + name);
            return s;
        }
    }

}
