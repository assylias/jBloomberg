/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.CorrelationID;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * An EventsManager that skips no-change events. For example, if the same "BID" price is received twice for the same
 * security, only the first one will be relayed to the listeners as a change.
 *
 * This implementation is thread safe.
 */
final class SynchronizedConflatedEventsManager implements EventsManager {

    private final SetMultimap<EventsKey, DataChangeListener> listeners = HashMultimap.<EventsKey, DataChangeListener>create();
    private final Map<EventsKey, String> tickers = new HashMap<>();
    Map<EventsKey, Object> lastKnownValue = new HashMap<>();

    @Override
    public synchronized void addEventListener(String ticker, CorrelationID id, RealtimeField field, DataChangeListener lst) {
        EventsKey key = EventsKey.of(id, field);
        listeners.put(key, lst);
        tickers.put(key, ticker);
    }

    @Override
    public synchronized void fireEvent(CorrelationID id, RealtimeField field, Object value) {
        EventsKey key = EventsKey.of(id, field);
        String ticker = tickers.get(key);
        Set<DataChangeListener> set = listeners.get(key);
        Object oldValue = lastKnownValue.get(key);
        if (value.equals(oldValue)) {
            return; //ignore identical value for same security and field
        }
        lastKnownValue.put(key, value);
        for (DataChangeListener lst : set) {
            lst.dataChanged(new DataChangeEvent(ticker, field.toString(), oldValue, value));
        }
    }
}
