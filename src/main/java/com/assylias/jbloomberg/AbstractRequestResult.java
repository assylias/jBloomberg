/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.google.common.collect.ImmutableSet;

import java.util.HashSet;
import java.util.Set;

/**
 * A basic implementation of the RequestResult interface, that only deals with errors.
 */
abstract class AbstractRequestResult implements RequestResult {

    /**
     * A set of security identifiers that returned error messages - can be empty
     */
    private final Set<String> securityErrors = new HashSet<>();
    /**
     * A set of fields that returned error messages - can be empty
     */
    private final Set<String> fieldErrors = new HashSet<>();

    @Override
    public synchronized boolean hasErrors() {
        return !(securityErrors.isEmpty() && fieldErrors.isEmpty());
    }

    @Override
    public synchronized Set<String> getFieldErrors() {
        return ImmutableSet.copyOf(fieldErrors);
    }

    @Override
    public synchronized Set<String> getSecurityErrors() {
        return ImmutableSet.copyOf(securityErrors);
    }

    synchronized void addSecurityError(String security) {
        securityErrors.add(security);
    }

    synchronized void addFieldError(String field) {
        fieldErrors.add(field);
    }
}
