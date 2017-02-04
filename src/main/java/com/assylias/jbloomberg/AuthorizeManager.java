/**
 * Zentech-Inc.com
 * Copyright (C) 2016 All Rights Reserved.
 */
package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.*;

import java.io.IOException;

/**
 * @author wujn
 * @version $Id AuthorizeManager.java, v 0.1 2016-10-11 17:43 wujn Exp $$
 */
public final class AuthorizeManager {
    /**
     * authorize success event
     */
    private static final Name AUTHORIZATION_SUCCESS = Name
            .getName("AuthorizationSuccess");
    /**
     * authorize failed event
     */
    private static final Name AUTHORIZATION_FAILURE = Name
            .getName("AuthorizationFailure");
    /**
     * get token success event
     */
    private static final Name TOKEN_SUCCESS = Name
            .getName("TokenGenerationSuccess");
    /**
     * get token failed event
     */
    private static final Name TOKEN_FAILURE = Name
            .getName("TokenGenerationFailure");

    /**
     * try open authapi service to get token
     *
     * @param session
     * @return
     * @throws IOException
     * @throws InterruptedException
     * @throws BloombergException
     */
    public synchronized Identity authorize(Session session) throws IOException, InterruptedException, BloombergException {

        if (!session.openService(BloombergServiceType.API_AUTHORIZATION.getUri())) {
            throw new BloombergException("can't open authapi service");
        }
        CorrelationID tokenId = new CorrelationID();
        EventQueue authEventQueue = new EventQueue();
        session.generateToken(tokenId, authEventQueue);
        int time = 0;
        while (true) {
            Event event = authEventQueue.nextEvent();
            if (event.eventType() == Event.EventType.TOKEN_STATUS) {
                time = 0;
                Identity identity = processToken(session, event);
                if (null == identity) {
                    throw new BloombergException("authorize failed");
                } else {
                    return identity;
                }
            } else if (event.eventType() == Event.EventType.TIMEOUT) {
                if (time++ > 10) {
                    throw new BloombergException("can't open authapi service");
                }
            }
        }
    }

    /**
     * authorize event processor
     *
     * @param session
     * @param event
     * @return
     * @throws BloombergException
     */
    private Identity processToken(Session session, Event event) throws BloombergException {
        try {
            MessageIterator msgIter = event.messageIterator();
            while (msgIter.hasNext()) {
                Message msg = msgIter.next();
                if (msg.messageType() == TOKEN_SUCCESS) {
                    Service authService = session.getService(BloombergServiceType.API_AUTHORIZATION.getUri());
                    Request authRequest = authService.createAuthorizationRequest();
                    authRequest.set("token", msg.getElementAsString("token"));
                    Identity identity = session.createIdentity();
                    session.sendAuthorizationRequest(authRequest, identity, new CorrelationID());
                    return identity;
                } else if (msg.messageType() == TOKEN_FAILURE) {
                    return null;
                }
            }
            return null;
        } catch (Throwable ex) {
            throw new BloombergException(ex.getMessage(), ex);
        }
    }
}
