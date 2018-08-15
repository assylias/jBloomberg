/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Request;

import static java.util.Objects.requireNonNull;

/**
 * This class enables to build a BloombergSearchData request while ensuring argument safety. The only parameter
 * which is allowed is just the domain, i.e. the saved bloomberg search name ("FI:CGB", if you were to create a search
 * for chinese bonds named CGB. the FI is the namespace and is implicitly added to the search name"
 * <p>
 * All methods, including the constructors, throw NullPointerException when null arguments are passed in.
 * <p>
 * Once the request has been built, the ReferenceRequestBuilder can be submitted to a BloombergSession.
 * <p>
 * <b>This class is not thread safe.</b>
 */
public final class BloombergSearchRequestBuilder extends AbstractRequestBuilder<BloombergSearchData> {

    //Required parameters
    private final String domain;
    private int limit = Integer.MIN_VALUE; //ignored if < 0

    /**
     * just takes 1 parameter which is the domain (i.e. the saved bsrch query name)
     */
    public BloombergSearchRequestBuilder(String domain) {
        this.domain = requireNonNull(domain, "domain must not be null");
    }

    @Override
    public String toString() {
        return "ExcelGetGridRequestBuilder{" + "domain=" + domain + "}";
   }

    @Override
    public BloombergServiceType getServiceType() {
        return BloombergServiceType.EXR_SERVICE;
    }

    @Override
    public BloombergRequestType getRequestType() {
        return BloombergRequestType.EXCELGETGRIDREQUEST_DATA;
    }

    /**
     * Limits the number of returned securities
     * @param limit maximum number of securities returned by the request, must be {@code > 0}
     * @throws IllegalArgumentException if {@code limit <= 0}
     */
    public BloombergSearchRequestBuilder maxSecurities(int limit) {
        if (limit <= 0) throw new IllegalArgumentException("limit should be > 0, received " + limit);
        this.limit = limit;
        return this;
    }

    @Override
    protected void buildRequest(Request request) {
        request.set("Domain", domain);
        if (limit > 0) {
            Element overridesElt = request.getElement("Overrides");
            Element override = overridesElt.appendElement();
            override.setElement("name", "LIMIT");
            override.setElement("value", String.valueOf(limit));
        }
    }

    @Override
    public ResultParser<BloombergSearchData> getResultParser() {
        return new BloombergSearchResultParser();
    }
}
