/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.CorrelationID;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An EventsManager that skips no-change events. For example, if the same "BID" is received twice for the same security,
 * only the first one will be relayed to the listeners as a change.
 *
 * This implementation is thread safe.
 */
final class ConcurrentConflatedEventsManager implements EventsManager {

    private final static Logger logger = LoggerFactory.getLogger(ConcurrentConflatedEventsManager.class);

    private final ConcurrentMap<EventsKey, Listeners> listenersMap = new ConcurrentHashMap<>();

    @Override
    public void addEventListener(String ticker, CorrelationID id, RealtimeField field, DataChangeListener lst) {
        logger.debug("addEventListener({}, {}, {}, {})", new Object[] {ticker, id, field, lst});
        EventsKey key = EventsKey.of(id, field);
        Listeners newListeners = new Listeners(ticker);
        Listeners listenersInMap = listenersMap.putIfAbsent(key, newListeners);
        if (listenersInMap == null) {
            listenersInMap = newListeners;
        }
        synchronized (key) { //make sure the addition to the set is visible
            listenersInMap.addListener(lst);
        }
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
        synchronized (key) { //we can do that because there is only one instance of each possible key
            if (!value.equals(lst.previousValue)) {
                evt = new DataChangeEvent(ticker, field.toString(), lst.previousValue, value);
                lst.previousValue = value;
            }
        }
        if (evt != null) lst.fireEvent(evt);
    }

    private static class Listeners {

        private final String ticker;
        private final Set<DataChangeListener> listeners = new HashSet<>();
        private Object previousValue;

        Listeners(String ticker) {
            this.ticker = ticker;
        }

        void addListener(DataChangeListener lst) {
            listeners.add(lst);
        }

        void fireEvent(DataChangeEvent evt) {
            for (DataChangeListener lst : listeners) {
                lst.dataChanged(evt);
            }
        }
    }
}
