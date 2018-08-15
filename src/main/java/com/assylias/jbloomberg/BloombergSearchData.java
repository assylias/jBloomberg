/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.google.common.collect.ImmutableSet;

import java.util.HashSet;
import java.util.Set;

/**
 * A class that represents the result returned from a Bloomberg saved SRCH query.  This is return as a list
 * given the only return values are the BB security codes from the search.
 *
 */
public final class BloombergSearchData extends AbstractRequestResult {

    /**
     * a hashset which has the list of securities that are returned from the search.
     */
    private final Set<String> data = new HashSet<>();

    @Override
    public synchronized boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public synchronized String toString() {
        StringBuilder sb = new StringBuilder("[DATA]");
        if (isEmpty()) {
            sb.append("{}");
        } else {
            sb.append("{").append(data).append("}");
        }
        if (!getSecurityErrors().isEmpty()) {
            sb.append("[SECURITY_ERRORS]").append(getSecurityErrors());
        }
        if (!getFieldErrors().isEmpty()) {
            sb.append("[FIELD_EXCEPTIONS]").append(getFieldErrors());
        }
        return sb.toString();
    }

    /**
     * Adds a value to the search results.
     */
    synchronized void add(String security) {
        data.add(security);
    }

    /**
     * returns a cloned copy of the results.
     */
    public Set<String> get() {
        return ImmutableSet.copyOf(data);
    }
}
