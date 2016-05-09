/*
 * Copyright (C) 2016 - present by Yann Le Tallec.
 * Please see distribution for license.
 */

package com.assylias.jbloomberg;

/**
 * A SubscriptionErrorListener is passed to a BloombergSession to be informed of error received after subscribing
 * to real time changes. An error can be a subscription failure due to the security being inactive for example.
 */
public interface SubscriptionErrorListener {

    /**
     * Invoked when an error occurs after subscribing to a real time data feed.
     * @param e the error received.
     */
    void onError(SubscriptionError e);
}
