/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.Request;
import com.bloomberglp.blpapi.Session;
import mockit.Mock;
import mockit.MockUp;

public class MockRequestBuilder<T extends AbstractRequestResult> extends MockUp<RequestBuilder<T>> {

    private BloombergServiceType serviceType;

    public MockRequestBuilder<?> serviceType(BloombergServiceType serviceType) {
        this.serviceType = serviceType;
        return this;
    }

    @Mock
    public BloombergServiceType getServiceType() {
        return serviceType;
    }

    public BloombergRequestType getRequestType() {
        return null;
    }

    public Request buildRequest(Session session) {
        return null;
    }

    public ResultParser<T> getResultParser() {
        return new StubResultParser<>(() -> {
            throw new UnsupportedOperationException("Not supported yet.");
        });
    }

}
