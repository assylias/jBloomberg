/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.assylias.jbloomberg.collection.SetTrie;
import com.bloomberglp.blpapi.CorrelationID;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
    private final ConcurrentMap<CorrelationID, SetTrie<RealtimeField>> subscribedFields = new ConcurrentHashMap<>();
    private final ConcurrentMap<EventsKey, Listeners> listenersMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<CorrelationID, SubscriptionErrorListener> errorListeners = new ConcurrentHashMap<>();

    @Override
    public void addEventListener(String ticker, CorrelationID id, Set<RealtimeField> fields, DataChangeListener lst) {
        logger.debug("addEventListener({}, {}, {}, {})", ticker, id, fields, lst);
        for (RealtimeField field : fields) {
            Set<RealtimeField> singleField = Collections.singleton(field);
            subscribedFields.computeIfAbsent(id, i -> SetTrie.create()).add(singleField);
            Listeners listenersInMap = listenersMap.computeIfAbsent(EventsKey.of(id, singleField), k -> new Listeners(ticker));
            listenersInMap.addListener(lst);
        }
    }

    @Override
    public void addEventMultiListener(String ticker, CorrelationID id, Set<RealtimeField> fields, DataChangeMultiListener lst) {
        logger.debug("addEventMultiListener({}, {}, {}, {})", ticker, id, fields, lst);
        subscribedFields.computeIfAbsent(id, i -> SetTrie.create()).add(fields);
        Listeners listenersInMap = listenersMap.computeIfAbsent(EventsKey.of(id, fields), k -> new Listeners(ticker));
        listenersInMap.addMultiListener(lst);
    }

    @Override
    public void fireEvents(CorrelationID id, Map<RealtimeField, Object> values) {
        final SetTrie<RealtimeField> setTrie = subscribedFields.get(id);
        final Set<RealtimeField> fields = values.keySet();
        final Set<Set<RealtimeField>> keys = Sets.union(Collections.singleton(fields), Sets.union(setTrie.getAllSubsetsOf(fields), setTrie.getAllSupersetsOf(fields)));

        for (Set<RealtimeField> key : keys) {
            Listeners lst = listenersMap.get(EventsKey.of(id, key));
            if (lst == null) {
                continue; //skip that event: nobody's listening anyway
            }
            String ticker = lst.ticker;
            List<DataChangeEvent> events = new LinkedList<>();
            synchronized (lst) {
                for (Map.Entry<RealtimeField, Object> entry : values.entrySet()) {
                    // we may have more fields than this subscriber asked for
                    if (key.contains(entry.getKey())) {
                        RealtimeField field = entry.getKey();
                        TypedObject newValue = TypedObject.of(entry.getValue());
                        TypedObject previousValue = lst.previousValues.get(field);
                        if (!newValue.equals(previousValue)) {
                            events.add(new DataChangeEvent(ticker, field.toString(), previousValue, newValue));
                            lst.previousValues.put(field, newValue);
                        }
                    }
                }
            }
            if (!events.isEmpty()) lst.fireEvents(events);
        }
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
        private final Set<DataChangeMultiListener> multiListeners = Collections.newSetFromMap(new ConcurrentHashMap<>());
        private final EnumMap<RealtimeField, TypedObject> previousValues = new EnumMap<>(RealtimeField.class);

        Listeners(String ticker) {
            this.ticker = ticker;
        }

        void addListener(DataChangeListener lst) {
            listeners.add(lst);
        }

        void addMultiListener(DataChangeMultiListener lst) {
            multiListeners.add(lst);
        }

        void fireEvents(List<DataChangeEvent> events) {
            for (DataChangeListener lst : listeners) {
                for (DataChangeEvent event : events) {
                    //(i)  if a listener gets stuck, the others can still make progress
                    //(ii) if a listener throws an exception, a new thread will be created
                    Future<?> f = fireListeners.submit(() -> lst.dataChanged(event));
                    monitorListenerExecution(f, lst, Collections.singletonList(event));
                }
            }
            for (DataChangeMultiListener lst : multiListeners) {
                Future<?> f = fireListeners.submit(() -> lst.dataChanged(events));
                monitorListenerExecution(f, lst, events);
            }
        }

        void monitorListenerExecution(Future<?> f, Object lst, List<DataChangeEvent> events) {
            fireListeners.submit(new Callable<Void>() {
                @Override public Void call() throws Exception {
                    try {
                        f.get(1, TimeUnit.SECONDS);
                    } catch (TimeoutException e) {
                        logger.warn("Slow listener {} has not processed event {} in one second", lst, events);
                    } catch (ExecutionException e) {
                        logger.error("Listener " + lst + " has thrown exception on event " + events, e.getCause());
                    }
                    return null;
                  }
            });
        }
    }
}
