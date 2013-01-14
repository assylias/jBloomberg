/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package assylias.jbloomberg;

import assylias.jbloomberg.DefaultBloombergSession;
import assylias.jbloomberg.BloombergServiceType;
import assylias.jbloomberg.RequestBuilder;
import assylias.jbloomberg.ResultParser;
import assylias.jbloomberg.AbstractResultParser;
import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Request;
import com.bloomberglp.blpapi.Session;
import mockit.Mock;
import mockit.MockUp;

/**
 *
 * @author Yann Le Tallec
 */
public class MockRequestBuilder extends MockUp<RequestBuilder> {

    private BloombergServiceType serviceType;

    public MockRequestBuilder serviceType(BloombergServiceType serviceType) {
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

    public ResultParser getResultParser() {
        return new AbstractResultParser() {

            @Override
            protected void parseResponseNoResponseError(Element response) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }
}
