/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.CorrelationID;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * An immutable, thread safe class that holds a pair of CorrelationID and RealtimeField to uniquely identify
 * subscription data. The factory method provided guarantees uniqueness so that == and equals will always return the
 * same result.
 */
final class EventsKey {

    private final static ConcurrentMap<EventsKey, EventsKey> keys = new ConcurrentHashMap<>();
    private final CorrelationID id;
    private final RealtimeField field;
    private final int hash;

    /**
     * Factory method to ensure that there is only one EventsKey instance associated with a given correlation ID and a
     * field.
     */
    public static EventsKey of(CorrelationID id, RealtimeField field) {
        return keys.computeIfAbsent(new EventsKey(id, field), Function.identity());
    }

    private EventsKey(CorrelationID id, RealtimeField field) {
        this.id = id;
        this.field = field;
        this.hash = getHashCode(id, field); //cached
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final EventsKey other = (EventsKey) obj;
        return Objects.equals(this.id, other.id) && this.field == other.field;
    }

    private int getHashCode(CorrelationID id, RealtimeField field) {
        return Objects.hash(id, field);
    }

    @Override
    public String toString() {
        return "key [id=" + id + ", field=" + field + ", hash=" + hash + "]";
    }
}
