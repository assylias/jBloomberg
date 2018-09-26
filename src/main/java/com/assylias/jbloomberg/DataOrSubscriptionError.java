package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.CorrelationID;
import com.google.common.base.Preconditions;

import java.util.Map;

public class DataOrSubscriptionError {
    private final CorrelationID id;
    private final Map<String, Object> data;
    private final SubscriptionError error;

    private DataOrSubscriptionError(final CorrelationID id, final Map<String, Object> data, final SubscriptionError error) {
        Preconditions.checkArgument(data != null ^ error != null);
        this.id = Preconditions.checkNotNull(id);
        this.data = data;
        this.error = error;
    }

    public static DataOrSubscriptionError of(final CorrelationID id, final Map<String, Object> data) {
        return new DataOrSubscriptionError(id, data, null);
    }

    public static DataOrSubscriptionError of(final CorrelationID id, final SubscriptionError error) {
        return new DataOrSubscriptionError(id, null, error);
    }

    public CorrelationID getCorrelationId() {
        return id;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public SubscriptionError getError() {
        return error;
    }

    public boolean isError() {
        return error != null;
    }
}
