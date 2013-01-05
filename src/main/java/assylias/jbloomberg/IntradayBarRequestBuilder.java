/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package assylias.jbloomberg;

import com.bloomberglp.blpapi.Request;
import com.bloomberglp.blpapi.Service;
import com.bloomberglp.blpapi.Session;
import com.google.common.base.Preconditions;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * This class enables to build an intraday historical request while ensuring argument safety. Typically, instead of passing
 * strings arguments (and typos) as with the standard Bloomberg API, the possible options used to override the behaviour
 * of the query have been wrapped in enums or relevant primitive types.
 * <p/>
 * All methods, including the constructors, throw NullPointerException when null arguments are passed in.
 * <p/>
 * Once the request has been built, the RequestBuilder can be submitted to a BloombergSession.
 */
public final class IntradayBarRequestBuilder implements RequestBuilder {

    //Required parameters
    private final String ticker;
    private final EventType eventType;
    private final DateTime startDateTime;
    private final DateTime endDateTime;
    //Optional parameters
    private int period = 1;
    private boolean fillInitialBar = false;
    private boolean adjustNormal = false;
    private boolean adjustAbnormal = false;
    private boolean adjustSplit = false;
    private boolean adjustDefault = true;

    /**
     * Creates a RequestBuilder with an event type TRADE. The Builder can be further customised with the provided
     * methods.
     *
     * @param ticker    a ticker for which data needs to be retrieved - must be valid Bloomberg symbol (for example: IBM
     *                  US Equity)
     * @param startDate the start of the date range (inclusive) for which to retrieve data
     * @param endDate   the end of the date range (inclusive) for which to retrieve data
     *
     * @throws NullPointerException     if any of the parameters is null
     * @throws IllegalArgumentException if the ticker is an empty string or if the start date is strictly after the end
     *                                  date
     */
    public IntradayBarRequestBuilder(String ticker, DateTime startDateTime, DateTime endDateTime) {
        this(ticker, EventType.TRADE, startDateTime, endDateTime);
    }

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
    public IntradayBarRequestBuilder(String ticker, EventType eventType, DateTime startDateTime, DateTime endDateTime) {
        this.startDateTime = Preconditions.checkNotNull(startDateTime, "The start date must not be null");
        this.endDateTime = Preconditions.checkNotNull(endDateTime, "The end date must not be null");
        this.ticker = Preconditions.checkNotNull(ticker, "The ticker must not be null");
        this.eventType = Preconditions.checkNotNull(eventType, "The event type must not be null");
        Preconditions.checkArgument(!startDateTime.isAfter(endDateTime), "The start date (%s) must not be after the end date (%s)", startDateTime, endDateTime);
        Preconditions.checkArgument(!ticker.isEmpty(), "The ticker must not be an empty string");
    }

    /**
     * @param period Determine the period of the output. Sets the length of each time bar in the response. Entered
     *               as a whole number, between 1 and 1440 in minutes. If omitted, the request will default to one
     *               minute. One minute is the lowest possible granularity.
     *
     * @throws IllegalArgumentException if period is not between 1 and 1440
     */
    public IntradayBarRequestBuilder period(int period) {
        Preconditions.checkArgument(period >= 1 && period <= 1440);
        this.period = period;
        return this;
    }

    /**
     * Default setting: false
     *
     * @param fillInitialBar If true, a bar contains the previous bar values if there was no tick during this time
     *                       interval.
     */
    public IntradayBarRequestBuilder fillInitialBar(boolean fillInitialBar) {
        this.fillInitialBar = fillInitialBar;
        return this;
    }

    /**
     * Default setting: true
     *
     * @param adjDefault If true, historical pricing is adjusted based on the DPDF<GO> BLOOMBERG PROFESSIONAL service
     *                   function.
     */
    public IntradayBarRequestBuilder adjustDefault(boolean adjDefault) {
        this.adjustDefault = adjDefault;
        return this;
    }

    /**
     * Default setting: false
     *
     * @param adjAbnormal If true, historical pricing is adjusted to reflect: Special Cash, Liquidation, Capital Gains,
     *                    Long-Term Capital Gains, Short-Term Capital Gains, Memorial, Return of Capital, Rights
     *                    Redemption, Miscellaneous, Return Premium, Preferred Rights Redemption, Proceeds/Rights,
     *                    Proceeds/Shares, Proceeds/ Warrants.
     */
    public IntradayBarRequestBuilder adjustAbnormalDistributions(boolean adjAbnormal) {
        this.adjustAbnormal = adjAbnormal;
        return this;
    }

    /**
     * Default setting: false
     *
     * @param adjNormal if true, historical pricing is adjusted to reflect: Regular Cash, Interim, 1st Interim, 2nd
     *                  Interim, 3rd Interim, 4th Interim, 5th Interim, Income, Estimated, Partnership Distribution,
     *                  Final, Interest on Capital, Distribution, Prorated.
     */
    public IntradayBarRequestBuilder adjustNormalDistributions(boolean adjNormal) {
        this.adjustNormal = adjNormal;
        return this;
    }

    /**
     * Default setting: false
     *
     * @param adjSplit if true, historical pricing and/or volume are adjusted to reflect: Spin-Offs, Stock
     *                 Splits/Consolidations, Stock Dividend/Bonus, Rights Offerings/ Entitlement.
     */
    public IntradayBarRequestBuilder adjustSplits(boolean adjSplit) {
        this.adjustSplit = adjSplit;
        return this;
    }

    @Override
    public String toString() {
        return "IntradayBarRequestBuilder{" + "ticker=" + ticker + ", eventType=" + eventType + ", startDateTime=" + startDateTime + ", endDateTime=" + endDateTime + ", period=" + period + ", fillInitialBar=" + fillInitialBar + ", adjNormal=" + adjustNormal + ", adjAbnormal=" + adjustAbnormal + ", adjSplit=" + adjustSplit + ", adjDefault=" + adjustDefault + '}';
    }

    @Override
    public DefaultBloombergSession.BloombergService getServiceType() {
        return DefaultBloombergSession.BloombergService.REFERENCE_DATA;
    }

    @Override
    public DefaultBloombergSession.BloombergRequest getRequestType() {
        return DefaultBloombergSession.BloombergRequest.INTRADAY_BAR;
    }

    @Override
    public Request buildRequest(Session session) {
        Service service = session.getService(getServiceType().getUri());
        Request request = service.createRequest(getRequestType().toString());
        buildRequest(request);
        return request;
    }

    private void buildRequest(Request request) {
        request.set("security", ticker);
        request.set("eventType", eventType.name());
        request.set("interval", period);
        request.set("gapFillInitialBar", fillInitialBar);
        request.set("adjustmentNormal", adjustNormal);
        request.set("adjustmentAbnormal", adjustAbnormal);
        request.set("adjustmentSplit", adjustSplit);
        request.set("adjustmentFollowDPDF", adjustDefault);

        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");

        request.set("startDateTime", startDateTime.toString(fmt));
        request.set("endDateTime", endDateTime.toString(fmt));
    }

    @Override
    public ResultParser getResultParser() {
        return new IntradayBarResultParser(ticker);
    }

    /**
     * Defines the field to be returned for historical intraday requests.
     */
    public static enum EventType {

        /**
         * Corresponds to LAST_PRICE
         */
        TRADE,
        /**
         * Depending on the exchange bid ticks will be returned as BID, BID_BEST or BEST_BID.
         */
        BID,
        /**
         * Depending on the exchange ask ticks will be returned as ASK, ASK_BEST or BEST_ASK.
         */
        ASK,
        /**
         * Depending on the exchange bid ticks will be returned as BID, BID_BEST or BEST_BID.
         */
        BID_BEST,
        /**
         * Depending on the exchange ask ticks will be returned as ASK, ASK_BEST or BEST_ASK.
         */
        ASK_BEST,
        /**
         * Depending on the exchange bid ticks will be returned as BID, BID_BEST or BEST_BID.
         */
        BEST_BID,
        /**
         * Depending on the exchange ask ticks will be returned as ASK, ASK_BEST or BEST_ASK.
         */
        BEST_ASK;
    }
}
