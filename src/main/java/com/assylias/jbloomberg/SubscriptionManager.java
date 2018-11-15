/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.CorrelationID;
import com.bloomberglp.blpapi.Identity;
import com.bloomberglp.blpapi.Subscription;
import com.bloomberglp.blpapi.SubscriptionList;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A class to which the main session object delegates the real time subscriptions management.
 */
final class SubscriptionManager {

    private final static Logger logger = LoggerFactory.getLogger(SubscriptionManager.class);
    private DefaultBloombergSession session;
    /**
     * A map that keeps track of what has been submitted to the session so far to better handle resubscriptions and
     * cancellations. All reads and writes must be done in synchronized blocks.
     */
    private final Map<String, SubscriptionHolder> subscriptionsByTicker = new HashMap<>();
    /**
     * A map that links CorrelationIDs and subscriptions.
     */
    private final ConcurrentMap<CorrelationID, SubscriptionHolder> subscriptionsById = new ConcurrentHashMap<>();
    /**
     * The queue that is used to transfer subscription data from Bloomberg to the interested parties
     */
    private final BlockingQueue<DataOrSubscriptionError> subscriptionDataQueue;
    /**
     * Everything runs in the same thread
     */
    private static final int NUM_THREADS = 1;
    /**
     * An executor to forward events from the queue to listeners
     */
    private final ExecutorService edt = Executors.newFixedThreadPool(NUM_THREADS, new ThreadFactory() {
        private final AtomicInteger number = new AtomicInteger();

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "Bloomberg EDT #" + number.incrementAndGet());
        }
    });
    /**
     * The Events manager that will forward events to the listeners
     */
    private final EventsManager eventsManager;

    public SubscriptionManager(BlockingQueue<DataOrSubscriptionError> subscriptionDataQueue, EventsManager eventsManager) {
        this.subscriptionDataQueue = subscriptionDataQueue;
        this.eventsManager = eventsManager;
    }

    /**
     * This method needs to be called to start the Subscription Manager. The session must be started when this method is
     * called.
     *
     * @param session a started Bloomberg session
     */
    synchronized void start(DefaultBloombergSession session) {
        logger.info("Starting the SubscriptionManager for {}", session);
        this.session = Preconditions.checkNotNull(session, "session can't be null");
        startDispatching();
    }

    synchronized void stop(DefaultBloombergSession stoppingSession) {
        if (session == null) {
            logger.info("Stopping the SubscriptionManager for {}", stoppingSession);
        } else if (session == stoppingSession) {
            logger.info("Stopping the SubscriptionManager for {}", session);
        } else {
            throw new IllegalStateException("The starting and stopping sessions are not the same: [start] " + session
                    + " [stop]" + stoppingSession);
        }
        edt.shutdownNow();
    }

    private void startDispatching() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        DataOrSubscriptionError dataOrError = subscriptionDataQueue.take();
                        CorrelationID id = dataOrError.getCorrelationId();
                        if (!dataOrError.isError()) {
                            EnumMap<RealtimeField, Object> data = new EnumMap<>(RealtimeField.class);
                            for (Map.Entry<String, Object> e : dataOrError.getData().entrySet()) {
                                if (RealtimeField.containsIgnoreCase(e.getKey())) {
                                    RealtimeField field = RealtimeField.valueOfIgnoreCase(e.getKey());
                                    data.put(field, e.getValue());
                                }
                            }
                            if (!data.isEmpty()) {
                                eventsManager.fireEvents(id, data);
                            }
                        } else {
                            SubscriptionError error = dataOrError.getError();
                            logger.info("Subscription error [{}]: {}", error.getTopic(), error.getDescription());
                            if ("SubscriptionFailure".equals(error.getType())) {
                                //we need to remove the subscription from our maps otherwise a resubscribe could throw an exception.
                                String ticker = error.getTopic();
                                subscriptionsByTicker.remove(ticker);
                                subscriptionsById.remove(id);
                            }
                            eventsManager.fireError(id, error);
                        } else {
                            logger.trace("Unknown RT Field: {}", data.getField());
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                logger.info("Exiting Subscription Manager dispatching loop");
            }
        };
        for (int i = 0; i < NUM_THREADS; i++) {
            edt.submit(r);
        }
    }

    /**
     * Updates the Bloomberg session to subscribe to the securities and fields specified in the builder.
     *
     * @param subscriptionBuilder the builder containing the details of the securities and fields to subscribe
     * @param identity a handle to the user whose privileges are to be used for this request
     *
     * @throws IllegalStateException if a started session has not been set before this method is called
     * @throws IOException           if there is a communication error on subscribe
     */
    synchronized void subscribe(SubscriptionBuilder subscriptionBuilder, Identity identity) throws IOException {
        if (session == null) {
            throw new IllegalStateException("Can't subscribe to a session before it is started");
        }

        SubscriptionList list;

        list = getReSubscriptionsList(subscriptionBuilder);
        if (!list.isEmpty()) {
            session.getBloombergSession().resubscribe(list);
        }

        list = getNewSubscriptionsList(subscriptionBuilder);
        if (!list.isEmpty()) {
            session.getBloombergSession().subscribe(list, identity);
        }
    }

    synchronized void subscribe(SubscriptionBuilder subscriptionBuilder) throws IOException {
      subscribe(subscriptionBuilder, null);
    }

    private SubscriptionList getNewSubscriptionsList(SubscriptionBuilder builder) {
        SubscriptionList list = new SubscriptionList();
        for (String ticker : builder.getSecurities()) {
            if (!subscriptionsByTicker.containsKey(ticker)) { //only include tickers that had no previous subscriptions
                list.add(getSubscription(ticker, builder));
            }
        }
        return list;
    }

    private Subscription getSubscription(String ticker, SubscriptionBuilder builder) {
        CorrelationID id = session.getNextCorrelationId();
        SubscriptionHolder sh = new SubscriptionHolder(id);
        logger.debug("Correlation id for {}: {}", ticker, sh.id);
        sh.update(builder);
        addListenersToEventsManager(builder, ticker, sh.id);
        subscriptionsByTicker.put(ticker, sh);
        subscriptionsById.put(sh.id, sh); //THIS IS THE ONLY PLACE WHERE WE WRITE TO THAT MAP

        return new Subscription(ticker, sh.getFieldsAsList(), getSubscriptionOptions(sh), sh.id);
    }

    private SubscriptionList getReSubscriptionsList(SubscriptionBuilder builder) {
        SubscriptionList list = new SubscriptionList();
        for (String ticker : builder.getSecurities()) {
            if (subscriptionsByTicker.containsKey(ticker)) { //only include tickers that have previously been subscribed
                list.add(getReSubscription(ticker, builder));
            }
        }
        return list;
    }

    private Subscription getReSubscription(String ticker, SubscriptionBuilder builder) {
        SubscriptionHolder sh = subscriptionsByTicker.get(ticker);
        sh.update(builder);
        addListenersToEventsManager(builder, ticker, sh.id);

        return new Subscription(ticker, sh.getFieldsAsList(), getSubscriptionOptions(sh), sh.id);
    }

    private static final List<String> USE_GMT = Collections.singletonList("useGMT");
    private List<String> getSubscriptionOptions(SubscriptionHolder sh) {
        return sh.throttle == 0 ? USE_GMT : Arrays.asList("useGMT", sh.getThrottleOption());
    }

    private void addListenersToEventsManager(SubscriptionBuilder builder, String ticker, CorrelationID id) {
        for (DataChangeListener lst : builder.getListeners()) {
            eventsManager.addEventListener(ticker, id, builder.getFields(), lst);
        }
        for (DataChangeMultiListener lst : builder.getMultiListeners()) {
            eventsManager.addEventMultiListener(ticker, id, builder.getFields(), lst);
        }
        eventsManager.onError(id, builder.getErrorListener());
    }

    private static class SubscriptionHolder {

        private final CorrelationID id;
        private final Set<RealtimeField> fields = EnumSet.noneOf(RealtimeField.class);
        private final Set<DataChangeListener> listeners = new HashSet<>();
        private double throttle = 0;

        public SubscriptionHolder(CorrelationID id) {
            this.id = id;
        }

        List<String> getFieldsAsList() {
            List<String> list = new ArrayList<>(fields.size());
            for (RealtimeField f : fields) {
                list.add(f.toString());
            }
            return list;
        }

        String getThrottleOption() {
            return "interval=" + throttle;
        }

        void update(SubscriptionBuilder builder) {
            fields.addAll(builder.getFields());
            listeners.addAll(builder.getListeners());
            throttle = builder.getThrottle();
        }
    }
}
