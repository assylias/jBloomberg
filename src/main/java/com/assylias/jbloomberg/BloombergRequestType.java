/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

/**
 * The names internally used by Bloomberg to identify the various types of requests - users don't need to use these
 * values directly.
 */
public enum BloombergRequestType {

    HISTORICAL_DATA("HistoricalDataRequest"),
    INTRADAY_TICK("IntradayTickRequest"),
    INTRADAY_BAR("IntradayBarRequest"),
    REFERENCE_DATA("ReferenceDataRequest"),
    PORTFOLIO_DATA("PortfolioDataRequest"),
    INSTRUMENT_LIST("instrumentListRequest");

    private final String requestName;

    BloombergRequestType(String requestName) {
        this.requestName = requestName;
    }

    /**
     * @return the type of request, for example "HistoricalDataRequest"
     */
    @Override
    public String toString() {
        return requestName;
    }
}
