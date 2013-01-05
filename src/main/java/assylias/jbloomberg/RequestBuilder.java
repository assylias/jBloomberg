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
 * <p>This interface has been made public for convenience but uses non public types and can't be implemented.
 */
public interface RequestBuilder {

    DefaultBloombergSession.BloombergService getServiceType();

    DefaultBloombergSession.BloombergRequest getRequestType();

    Request buildRequest(Session session);

    ResultParser getResultParser();
}
