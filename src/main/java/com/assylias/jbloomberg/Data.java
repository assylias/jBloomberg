/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.CorrelationID;

/**
 *
 * This class is use to hold the data returned by subscriptions.
 */
final class Data {
    private final CorrelationID correlationId;
    private final String field;
    private final Object value;

    Data(CorrelationID correlationId, String field, Object value) {
        this.correlationId = correlationId;
        this.field = field;
        this.value = value;
    }

    public CorrelationID getCorrelationId() {
        return correlationId;
    }

    public String getField() {
        return field;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "{id=" + correlationId + ", " + field + "=" + value + '}';
    }
}
