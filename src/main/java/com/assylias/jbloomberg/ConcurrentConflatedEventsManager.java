/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.assylias.bigblue.utils.TypedObject;
import com.bloomberglp.blpapi.CorrelationID;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
            Thread t = new Thread(r, "Bloomberg Listeners Thread #" + number.incrementAndGet());
            t.setDaemon(true); //daemon to allow JVM exit
            return t;
        }
    });
    private final ConcurrentMap<EventsKey, Listeners> listenersMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<CorrelationID, SubscriptionErrorListener> errorListeners = new ConcurrentHashMap<>();

    @Override
    public void addEventListener(String ticker, CorrelationID id, RealtimeField field, DataChangeListener lst) {
        logger.debug("addEventListener({}, {}, {}, {})", new Object[]{ticker, id, field, lst});
        EventsKey key = EventsKey.of(id, field);
        Listeners listenersInMap = listenersMap.computeIfAbsent(key, k -> new Listeners(ticker));
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

    @Override
    public void fireError(CorrelationID id, SubscriptionError error) {
        SubscriptionErrorListener lst = errorListeners.get(id);
        if (lst != null) {
            Future<?> f = fireListeners.submit(() -> lst.onError(error));
            monitorListenerExecution(f, lst, error);
        }
    }

    void monitorListenerExecution(Future<?> f, SubscriptionErrorListener lst, SubscriptionError error) {
        fireListeners.submit(new Callable<Void>() {
            @Override public Void call() throws Exception {
                try {
                    f.get(1, TimeUnit.SECONDS);
                } catch (TimeoutException e) {
                    logger.warn("Slow error listener {} has not processed error {} in one second", lst, error);
                } catch (ExecutionException e) {
                    logger.error("Listener " + lst + " has thrown exception on error " + error, e.getCause());
                }
                return null;
              }
        });
    }

    @Override
    public void onError(CorrelationID id, SubscriptionErrorListener lst) {
        errorListeners.put(id, lst);
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
                monitorListenerExecution(f, lst, evt);
            }
        }

        void monitorListenerExecution(Future<?> f, DataChangeListener lst, DataChangeEvent evt) {
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
