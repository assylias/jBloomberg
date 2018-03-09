/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.Request;
import com.google.common.base.Preconditions;
import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

/**
 * This class enables to build an IntradayBarData historical request while ensuring argument safety. Typically, instead
 * of passing strings arguments (and typos) as with the standard Bloomberg API, the possible options used to override the behaviour
 * of the query have been wrapped in enums or relevant primitive types.
 * <p>
 * All methods, including the constructors, throw NullPointerException when null arguments are passed in.
 * <p>
 * Once the request has been built, the RequestBuilder can be submitted to a BloombergSession.
 * <p>
 * <b>This class is not thread safe.</b>
 */
public final class IntradayBarRequestBuilder extends AbstractIntradayRequestBuilder<IntradayBarData> {

    //Optional parameters
    private int period = 1;
    private boolean fillInitialBar = false;
    private boolean adjustNormal = false;
    private boolean adjustAbnormal = false;
    private boolean adjustSplit = false;
    private boolean usePricingDefaults = true;

    /**
     * Creates a RequestBuilder with an event type TRADE. The Builder can be further customised with the provided
     * methods. By default, this RequestBuilder uses Bloomberg's Pricing Defaults (accessible via DPDF&lt;GO&gt;).
     *
     * @param ticker        a ticker for which data needs to be retrieved - must be valid Bloomberg symbol (for example:
     *                      IBM US Equity)
     * @param startDateTime the start of the date range (inclusive) for which to retrieve data
     * @param endDateTime   the end of the date range (inclusive) for which to retrieve data
     *
     * @throws NullPointerException     if any of the parameters is null
     * @throws IllegalArgumentException if the ticker is an empty string or if the start date is strictly after the end
     *                                  date
     */
    public IntradayBarRequestBuilder(String ticker, OffsetDateTime startDateTime, OffsetDateTime endDateTime) {
        this(ticker, IntradayBarEventType.TRADE, startDateTime, endDateTime);
    }

    /**
     * Creates a RequestBuilder with standard options. The Builder can be further customised with the provided
     * methods. By default, this RequestBuilder uses Bloomberg's Pricing Defaults (accessible via DPDF&lt;GO&gt;).
     * <p>
     * @param ticker        a ticker for which data needs to be retrieved - must be valid Bloomberg symbol (for example:
     *                      IBM US Equity)
     * @param eventType     the eventType to retrieve for the selected ticker
     * @param startDateTime the start of the date range (inclusive) for which to retrieve data
     * @param endDateTime   the end of the date range (inclusive) for which to retrieve data
     * <p>
     * @throws NullPointerException     if any of the parameters is null
     * @throws IllegalArgumentException if the ticker is an empty string or if the start date is strictly after the end
     *                                  date
     */
    public IntradayBarRequestBuilder(String ticker, IntradayBarEventType eventType, OffsetDateTime startDateTime, OffsetDateTime endDateTime) {
        super(ticker, eventType.toString(), startDateTime, endDateTime);
    }

    /**
     * @param period   Determine the period of the output. Sets the length of each time bar in the response. Once
     *                 rounded, it needs to represent a duration between 1 and 1440 in minutes. If omitted, the request will default
     *                 to one minute. One minute is the lowest possible granularity.
     * @param timeUnit The time unit in which period is expressed
     *
     * @throws IllegalArgumentException if period is not between 1 and 1440 minutes
     */
    public IntradayBarRequestBuilder period(int period, TimeUnit timeUnit) {
        int minutes = (int) TimeUnit.MINUTES.convert(period, timeUnit);
        Preconditions.checkArgument(minutes >= 1 && minutes <= 1440);
        this.period = minutes;
        return this;
    }

    /**
     * Populate an empty bar with previous value. A bar contains the previous bar values if there was no tick during
     * this time interval.
     */
    public IntradayBarRequestBuilder fillInitialBar() {
        this.fillInitialBar = true;
        return this;
    }

    /**
     * No price adjustment will be made for splits, distributions etc., regardless of the DPDF&lt;GO&gt; settings.
     */
    public IntradayBarRequestBuilder ignorePricingDefaults() {
        this.usePricingDefaults = false;
        return this;
    }

    /**
     * Adjust historical pricing to reflect: Special Cash, Liquidation, Capital Gains, Long-Term Capital Gains,
     * Short-Term Capital Gains, Memorial, Return of Capital, Rights Redemption, Miscellaneous, Return Premium,
     * Preferred Rights Redemption, Proceeds/Rights, Proceeds/Shares, Proceeds/ Warrants.
     * <p>
     * All other adjustments in the the DPDF&lt;GO&gt; settings are ignored, unless explicitly set with the other methods.
     */
    public IntradayBarRequestBuilder adjustAbnormalDistributions() {
        this.adjustAbnormal = true;
        this.usePricingDefaults = false;
        return this;
    }

    /**
     * Adjust historical pricing to reflect: Regular Cash, Interim, 1st Interim, 2nd Interim, 3rd Interim, 4th Interim,
     * 5th Interim, Income, Estimated, Partnership Distribution, Final, Interest on Capital, Distribution, Prorated.
     * <p>
     * All other adjustments in the the DPDF&lt;GO&gt; settings are ignored, unless explicitly set with the other methods.
     */
    public IntradayBarRequestBuilder adjustNormalDistributions() {
        this.adjustNormal = true;
        this.usePricingDefaults = false;
        return this;
    }

    /**
     * Adjust historical pricing and/or volume to reflect: Spin-Offs, Stock Splits/Consolidations, Stock Dividend/Bonus,
     * Rights Offerings/ Entitlement.
     * <p>
     * All other adjustments in the the DPDF&lt;GO&gt; settings are ignored, unless explicitly set with the other methods.
     */
    public IntradayBarRequestBuilder adjustSplits() {
        this.adjustSplit = true;
        this.usePricingDefaults = false;
        return this;
    }

    @Override
    public String toString() {
        return "IntradayBarRequestBuilder{" + super.toString() + ", period=" + period + ", fillInitialBar=" + fillInitialBar
                + ", adjNormal=" + adjustNormal + ", adjAbnormal=" + adjustAbnormal + ", adjSplit=" + adjustSplit
                + ", usePricingDefaults=" + usePricingDefaults + '}';
    }

    @Override
    protected void buildRequest(Request request) {
        super.buildRequest(request);
        request.set("eventType", getEventType());
        request.set("interval", period);
        request.set("gapFillInitialBar", fillInitialBar);
        request.set("adjustmentNormal", adjustNormal);
        request.set("adjustmentAbnormal", adjustAbnormal);
        request.set("adjustmentSplit", adjustSplit);
        request.set("adjustmentFollowDPDF", usePricingDefaults);
    }

    @Override
    public BloombergRequestType getRequestType() {
        return BloombergRequestType.INTRADAY_BAR;
    }

    @Override
    public ResultParser<IntradayBarData> getResultParser() {
        return new IntradayBarResultParser(getTicker());
    }

    /**
     * Defines the field to be returned for historical intraday bar requests.
     */
    public enum IntradayBarEventType {

        /**
         * Corresponds to LAST_PRICE
         */
        TRADE, /**
         * Depending on the exchange bid ticks will be returned as BID, BID_BEST or BEST_BID.
         */
        BID, /**
         * Depending on the exchange ask ticks will be returned as ASK, ASK_BEST or BEST_ASK.
         */
        ASK, /**
         * Depending on the exchange bid ticks will be returned as BID, BID_BEST or BEST_BID.
         */
        BID_BEST, /**
         * Depending on the exchange ask ticks will be returned as ASK, ASK_BEST or BEST_ASK.
         */
        ASK_BEST, /**
         * Depending on the exchange bid ticks will be returned as BID, BID_BEST or BEST_BID.
         */
        BEST_BID, /**
         * Depending on the exchange ask ticks will be returned as ASK, ASK_BEST or BEST_ASK.
         */
        BEST_ASK;
    }
}
