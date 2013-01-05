/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package assylias.jbloomberg;

import com.bloomberglp.blpapi.CorrelationID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
        EventsKey key = new EventsKey(id, field);
        EventsKey unique = keys.putIfAbsent(key, key);
        return unique != null ? unique : key;
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

    /**
     * Assumes that obj is of the right type and is not null
     */
    @Override
    public boolean equals(Object obj) {
        final EventsKey other = (EventsKey) obj;
        if (this.hash != other.hash) {
            return false;
        }
        if (!this.id.equals(other.id)) {
            return false;
        }
        if (this.field != other.field) {
            return false;
        }
        return true;
    }

    private int getHashCode(CorrelationID id, RealtimeField field) {
        int hash = 7;
        hash = 89 * hash + id.hashCode();
        hash = 89 * hash + field.hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return "key [id=" + id + ", field=" + field + ", hash=" + hash + "]";
    }
}
