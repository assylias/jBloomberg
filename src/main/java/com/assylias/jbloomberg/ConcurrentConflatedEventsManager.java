/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.assylias.fund.TypedObject;
import com.bloomberglp.blpapi.CorrelationID;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An EventsManager that skips no-change events. For example, if the same "BID" is received twice for the same security,
 * only the first one will be relayed to the listeners as a change.
 *
 * This implementation is thread safe.
 */
final class ConcurrentConflatedEventsManager implements EventsManager {

    private static final Logger logger = LoggerFactory.getLogger(ConcurrentConflatedEventsManager.class);
    private static final ExecutorService fireListeners = Executors.newFixedThreadPool(10, new ThreadFactory() {
        private final AtomicInteger number = new AtomicInteger();

        @Override public Thread newThread(Runnable r) {
            return new Thread(r, "Bloomberg Listeners Thread #" + number.incrementAndGet());
        }
    });
    private final ConcurrentMap<EventsKey, Listeners> listenersMap = new ConcurrentHashMap<>();

    @Override
    public void addEventListener(String ticker, CorrelationID id, RealtimeField field, DataChangeListener lst) {
        logger.debug("addEventListener({}, {}, {}, {})", new Object[]{ticker, id, field, lst});
        EventsKey key = EventsKey.of(id, field);
        Listeners newListeners = new Listeners(ticker);
        Listeners listenersInMap = listenersMap.putIfAbsent(key, newListeners);
        if (listenersInMap == null) {
            listenersInMap = newListeners;
        }
        listenersInMap.addListener(lst);
    }

    @Override
    public void fireEvent(CorrelationID id, RealtimeField field, Object value) {
        final EventsKey key = EventsKey.of(id, field);
        Listeners lst = listenersMap.get(key);
        if (lst == null) {
            return; //skip that event: nobody's listening anyway
        }
        String ticker = lst.ticker;
        DataChangeEvent evt = null;
        TypedObject newValue = TypedObject.of(value);
        synchronized (lst) {
            if (!newValue.equals(lst.previousValue)) {
                evt = new DataChangeEvent(ticker, field.toString(), lst.previousValue, newValue);
                lst.previousValue = newValue;
            }
        }
        if (evt != null) lst.fireEvent(evt);
    }

    private static class Listeners {

        private final String ticker;
        //Using a set so that a listener that registers twice is only called once
        private final Set<DataChangeListener> listeners = Collections.newSetFromMap(new ConcurrentHashMap<>());
        private TypedObject previousValue;

        Listeners(String ticker) {
            this.ticker = ticker;
        }

        void addListener(DataChangeListener lst) {
            listeners.add(lst);
        }

        void fireEvent(DataChangeEvent evt) {
            for (DataChangeListener lst : listeners) {
                //(i)  if a listener gets stuck, the others can still make progress
                //(ii) if a listener throws an exception, a new thread will be created
                Future<?> f = fireListeners.submit(() -> lst.dataChanged(evt));
                fireListeners.submit(new Callable<Void>() {
                    @Override public Void call() throws Exception {
                        try {
                            f.get(1, TimeUnit.SECONDS);
                        } catch (TimeoutException e) {
                            logger.warn("Slow listener {} has not processed event {} in one second", lst, evt);
                        } catch (ExecutionException e) {
                            logger.error("Listener " + lst + " has thrown exception on event " + evt, e.getCause());
                        }
                        return null;
                    }
                });
            }
        }
    }
}
