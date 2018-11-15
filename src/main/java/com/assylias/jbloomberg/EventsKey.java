/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.CorrelationID;

import java.util.Set;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * An immutable, thread safe class that holds a pair of {@link CorrelationID} and {@link Set<RealtimeField>} to uniquely
 * identify subscription data. The factory method provided guarantees uniqueness so that == and equals will always return the
 * same result.
 */
final class EventsKey {

    private final static ConcurrentMap<EventsKey, EventsKey> keys = new ConcurrentHashMap<>();
    private final CorrelationID id;
    private final Set<RealtimeField> fields;
    private final int hash;

    /**
     * Factory method to ensure that there is only one EventsKey instance associated with a given correlation ID and
     * fields.
     */
    public static EventsKey of(CorrelationID id, Set<RealtimeField> fields) {
        return keys.computeIfAbsent(new EventsKey(id, fields), Function.identity());
    }

    private EventsKey(CorrelationID id, Set<RealtimeField> fields) {
        this.id = id;
        this.fields = fields;
        this.hash = getHashCode(id, fields); //cached
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
        return Objects.equals(this.id, other.id) && this.fields.equals(other.fields);
    }

    private int getHashCode(CorrelationID id, Set<RealtimeField> fields) {
        return Objects.hash(id, fields);
    }

    @Override
    public String toString() {
        return "key [id=" + id + ", fields=" + fields + ", hash=" + hash + "]";
    }
}
