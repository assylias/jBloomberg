/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.Datetime;
import com.bloomberglp.blpapi.Request;
import com.google.common.base.Preconditions;
import java.time.ZonedDateTime;

/**
 * This is the base class to build intraday historical requests.
 */
abstract class AbstractIntradayRequestBuilder<T extends RequestResult> extends AbstractRequestBuilder<T> {

    //Required parameters
    private final String ticker;
    private final String eventType;
    private final ZonedDateTime startDateTime;
    private final ZonedDateTime endDateTime;

    /**
     * Creates a RequestBuilder with standard options. The Builder can be further customised with the provided
     * methods.
     * <p/>
     * @param ticker    a ticker for which data needs to be retrieved - must be valid Bloomberg symbol (for example: IBM
     *                  US Equity)
     * @param eventType the eventType to retrieve for the selected ticker
     * @param startDate the start of the date range (inclusive) for which to retrieve data
     * @param endDate   the end of the date range (inclusive) for which to retrieve data
     * <p/>
     * @throws NullPointerException     if any of the parameters is null
     * @throws IllegalArgumentException if the ticker is an empty string or if the start date is strictly after the end
     *                                  date
     */
    protected AbstractIntradayRequestBuilder(String ticker, String eventType, ZonedDateTime startDateTime, ZonedDateTime endDateTime) {
        this.startDateTime = Preconditions.checkNotNull(startDateTime, "The start date must not be null");
        this.endDateTime = Preconditions.checkNotNull(endDateTime, "The end date must not be null");
        this.ticker = Preconditions.checkNotNull(ticker, "The ticker must not be null");
        this.eventType = Preconditions.checkNotNull(eventType, "The event type must not be null");
        Preconditions.checkArgument(!startDateTime.isAfter(endDateTime), "The start date (%s) must not be after the end date (%s)", startDateTime, endDateTime);
        Preconditions.checkArgument(!ticker.isEmpty(), "The ticker must not be an empty string");
    }

    @Override
    public BloombergServiceType getServiceType() {
        return BloombergServiceType.REFERENCE_DATA;
    }

    @Override
    protected void buildRequest(Request request) {
        request.set("security", ticker);
        request.set("startDateTime", getDateTime(startDateTime));
        request.set("endDateTime", getDateTime(endDateTime));
    }

    String getEventType() {
        return eventType;
    }

    String getTicker() {
        return ticker;
    }

    @Override
    public String toString() {
        return "ticker=" + ticker + ", eventType=" + eventType + ", startDateTime=" + startDateTime + ", endDateTime=" + endDateTime;
    }

    public static Datetime getDateTime(ZonedDateTime d) {
      Datetime dt = new Datetime();
      dt.setDatetimeTz(d.getYear(), d.getMonthValue(), d.getDayOfMonth(), d.getHour(), d.getMinute(), d.getSecond(), d.getNano() / 1_000_000,
              d.getOffset().getTotalSeconds() / 60);
      return dt;
    }
}
