/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

/**
 * Exception thrown to signal that an error occurred while calling the Bloomberg API.<br>
 * This can happen for example if there is no Bloomberg terminal installed on the machine, if the user is not logged on,
 * if the connection gets shutdown, if a malformed query is sent to the API etc.
 */
public class BloombergException extends Exception {

    public BloombergException(String message) {
        super(message);
    }

    public BloombergException(String message, Throwable cause) {
        super(message, cause);
    }
}
