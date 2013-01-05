/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package assylias.jbloomberg;

import com.bloomberglp.blpapi.CorrelationID;
import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Event;
import com.bloomberglp.blpapi.EventHandler;
import com.bloomberglp.blpapi.Message;
import com.bloomberglp.blpapi.Name;
import com.bloomberglp.blpapi.Session;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of EventHandler. This is where all the messages are received from the Bloomberg session and
 * forwarded to the relevant parsers for requests or to a queue for subscriptions.
 */
final class BloombergEventHandler implements EventHandler {

    private final static Logger logger = LoggerFactory.getLogger(BloombergEventHandler.class);
    private final static Name SESSION_STARTED = new Name("SessionStarted");
    private final BlockingQueue<Data> subscriptionDataQueue;
    private final Map<CorrelationID, ResultParser> parsers = new ConcurrentHashMap<>();
    private volatile Runnable runOnSessionStarted;

    public BloombergEventHandler(BlockingQueue<Data> subscriptionDataQueue) {
        this.subscriptionDataQueue = subscriptionDataQueue;
    }

    @Override
    public void processEvent(Event event, Session session) {
        try {
            EventTypeEnum type = EventTypeEnum.get(event);
            switch (type) {
                case SESSION_STATUS:
                    for (Message msg : event) {
                        logger.debug("[{}] {}", type, msg);
                        if (msg.messageType().equals(SESSION_STARTED)) {
                            fireSessionStarted(session);
                        }
                    }
                    break;
                case PARTIAL_RESPONSE:
                    for (Message msg : event) {
                        logger.trace("[{}] {}", type, msg);
                        CorrelationID cId = msg.correlationID();
                        ResultParser parser = parsers.get(cId);
                        if (parser != null) {
                            parser.addMessage(msg);
                        }
                    }
                    break;
                case RESPONSE:
                    Set<CorrelationID> endOfTransmission = new HashSet<>();
                    for (Message msg : event) {
                        logger.trace("[{}] {}", type, msg);
                        CorrelationID cId = msg.correlationID();
                        ResultParser parser = parsers.get(cId);
                        if (parser != null) {
                            endOfTransmission.add(cId);
                            parser.addMessage(msg);
                        }
                    }
                    for (CorrelationID cId : endOfTransmission) {
                        parsers.get(cId).noMoreMessages();
                        parsers.remove(cId); //remove from the map - not needed any longer.
                    }
                    break;
                case SUBSCRIPTION_DATA:
                    for (Message msg : event) {
                        CorrelationID id = msg.correlationID();
                        int numFields = msg.asElement().numElements();
                        for (int i = 0; i < numFields; ++i) {
                            Element field = msg.asElement().getElement(i);
                            if (!field.isNull()) {
                                Data data = new Data(id, field.name().toString(), BloombergUtils.getSpecificObjectOf(field));
                                try {
                                    subscriptionDataQueue.put(data);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                    return; //ignore the rest
                                }
                                logger.trace("[SUBS_DATA] {}", data);
                            }
                        }
                    }
                    break;
                default:
                    for (Message msg : event) {
                        logger.debug("[{}] {}", type, msg);
                    }
            }
            //catch all - this code will run in one of the Bloomberg API's threads so we have no way to catch exceptions otherwise
        } catch (Exception e) {
            logger.error("Error in Bloomberg EventHandler: " + e.getMessage(), e);
            throw e; //rethrow in case the Bloomberg API needs to see it
        }
    }

    protected void fireSessionStarted(Session session) {
        runOnSessionStarted.run();
    }

    /**
     *
     * @param runOnSessionStarted this runnable will be run as soon as the session is started
     */
    void onSessionStarted(Runnable runOnSessionStarted) {
        this.runOnSessionStarted = runOnSessionStarted;
    }

    void setParser(CorrelationID requestId, ResultParser parser) {
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

//        abstract void processEvent();
        static EventTypeEnum get(Event evt) {
            return map.get(evt.eventType());
        }
    }
}
