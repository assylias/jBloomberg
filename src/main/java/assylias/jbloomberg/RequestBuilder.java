/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package assylias.jbloomberg;

import com.bloomberglp.blpapi.Request;
import com.bloomberglp.blpapi.Session;

/**
 * An interface used to create static or historical requests: users of this API should use existing implementations of
 * this interface.
 * <p>This interface has been made public for convenience but users should not need to implement it.
 */
public interface RequestBuilder<T extends RequestResult> {

    /**
     *
     * @return the service used by this request
     */
    BloombergServiceType getServiceType();

    /**
     *
     * @return the type of request (for example, historical data or intraday bars)
     */
    BloombergRequestType getRequestType();

    /**
     *
     * @param session the session to which the request will be sent
     * @return a properly built request that can be submitted to the session
     */
    Request buildRequest(Session session);

    /**
     *
     * @return the parser that should be used to parse the result returned by the Bloomberg session
     */
    ResultParser<T> getResultParser();
}
