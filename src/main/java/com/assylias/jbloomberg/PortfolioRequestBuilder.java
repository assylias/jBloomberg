/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.Identity;
import com.bloomberglp.blpapi.Request;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;

/**
 * This class is used to query portfolio data (e.g. portfolio constituents and weights). The portfolio ID to be used in the constructors can be found on the
 * PRTU screen: click on a portfolio and its ID will be shown at the top of the screen (it looks like Uxxxxx-xx and the yellow key is "Client").
 * <p>
 * All methods, including the constructors, throw NullPointerException when null arguments are passed in.
 * <p>
 * Once the request has been built, the PortfolioRequestBuilder can be submitted to a BloombergSession.
 * <p>
 * <b>This class is not thread safe.</b>
 */
public final class PortfolioRequestBuilder extends AbstractRequestBuilder<ReferenceData> {

    //Delegates most of the work to ReferenceRequestBuilder as most of the internal mechanics is similar
    private final ReferenceRequestBuilder rrb;

    private Identity identity = null;

    /**
     * Equivalent to calling
     * <code> new PortfolioRequestBuilder(Arrays.asList(portfolio), Arrays.asList(field));
     * </code>
     */
    public PortfolioRequestBuilder(String portfolio, String field) {
        this(Arrays.asList(portfolio), Arrays.asList(field));
    }

    /**
     * Equivalent to calling
     * <code> new PortfolioRequestBuilder(Arrays.asList(portfolio), fields);
     * </code>
     */
    public PortfolioRequestBuilder(String portfolio, Collection<String> fields) {
        this(Arrays.asList(portfolio), fields);
    }

    /**
     * Equivalent to calling
     * <code> new PortfolioRequestBuilder(tickers, Arrays.asList(field));
     * </code>
     */
    public PortfolioRequestBuilder(Collection<String> portfolios, String field) {
        this(portfolios, Arrays.asList(field));
    }

    /**
     * Creates a PortfolioRequestBuilder. The fields parameters can be overriden with the provided methods.
     * <p>
     * @param portfolios a collection of portfolio identifiers for which data needs to be retrieved - e.g. "Uxxxx-x Client"
     * @param fields  a collection of Bloomberg fields to retrieve for each portfolio
     * <p>
     * @throws NullPointerException     if any of the parameters is null or if the collections contain null items
     * @throws IllegalArgumentException if any of the collections is empty or contains empty strings
     */
    public PortfolioRequestBuilder(Collection<String> portfolios, Collection<String> fields) {
        rrb = new ReferenceRequestBuilder(portfolios, fields);
    }

    public PortfolioRequestBuilder at(LocalDate date) {
        rrb.addOverride("REFERENCE_DATE", date.format(BB_REQUEST_DATE_FORMATTER));
        return this;
    }

    /**
     * Specify the {@link Identity} used to make request
     *
     * @param identity Identity to use when making API requests
     */
    public PortfolioRequestBuilder withIdentity(Identity identity) {
        this.identity = identity;
        return this;
    }

    @Override
    public String toString() {
        return "PortfolioRequestBuilder: " + rrb.toString();
    }

    @Override
    public BloombergServiceType getServiceType() {
        return BloombergServiceType.REFERENCE_DATA;
    }

    @Override
    public BloombergRequestType getRequestType() {
        return BloombergRequestType.PORTFOLIO_DATA;
    }

    @Override
    public Identity getIdentity() {
        return identity;
    }

    @Override
    protected void buildRequest(Request request) {
        rrb.buildRequest(request);
    }

    @Override
    public ResultParser<ReferenceData> getResultParser() {
        return new ReferenceResultParser();
    }
}
