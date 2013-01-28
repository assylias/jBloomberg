/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package assylias.jbloomberg;

import com.bloomberglp.blpapi.Request;
import com.google.common.base.Preconditions;
import org.joda.time.DateTime;

/**
 * This class enables to build an IntradayTickData historical request while ensuring argument safety. Typically, instead
 * of
 * passing
 * strings arguments (and typos) as with the standard Bloomberg API, the possible options used to override the behaviour
 * of the query have been wrapped in enums or relevant primitive types.
 * <p/>
 * All methods, including the constructors, throw NullPointerException when null arguments are passed in.
 * <p/>
 * Once the request has been built, the RequestBuilder can be submitted to a BloombergSession.
 * <p/>
 * <b>This class is not thread safe.</b>
 */
public class IntradayTickRequestBuilder extends AbstractIntradayRequestBuilder {

    //Optional parameters
    private boolean includeConditionCodes;
    private boolean includeNonPlottable;
    private boolean includeExchangeCodes;
    private boolean includeBrokerCodes;
    private boolean includeRpsCodes;
    private boolean includeBicMicCodes;

    /**
     * Creates a RequestBuilder with an event type TRADE. The Builder can be further customised with the provided
     * methods.
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
    public IntradayTickRequestBuilder(String ticker, DateTime startDateTime, DateTime endDateTime) {
        this(ticker, IntradayTickEventType.TRADE, startDateTime, endDateTime);
    }

    /**
     * Creates a RequestBuilder with standard options. The Builder can be further customised with the provided
     * methods.
     * <p/>
     * @param ticker        a ticker for which data needs to be retrieved - must be valid Bloomberg symbol (for example:
     *                      IBM US Equity)
     * @param eventType     the eventType to retrieve for the selected ticker
     * @param startDateTime the start of the date range (inclusive) for which to retrieve data
     * @param endDateTime   the end of the date range (inclusive) for which to retrieve data
     * <p/>
     * @throws NullPointerException     if any of the parameters is null
     * @throws IllegalArgumentException if the ticker is an empty string or if the start date is strictly after the end
     *                                  date
     */
    public IntradayTickRequestBuilder(String ticker, IntradayTickEventType eventType, DateTime startDateTime, DateTime endDateTime) {
        super(ticker, eventType.toString(), startDateTime, endDateTime);
    }

    /**
     * Include any condition codes that may be associated to a tick. Condition codes identify extraordinary trading and
     * quoting circumstances. Condition codes are returned as a comma delimited list of exchange condition codes
     * associated with the event. Review QR<GO> for more information on each code returned.
     *
     * @param includeConditionCodes If true, return any condition codes that may be associated to a tick.
     */
    public IntradayTickRequestBuilder includeConditionCodes() {
        this.includeConditionCodes = true;
        return this;
    }

    /**
     * Include all ticks, including those with condition codes. This override is part of the specification but has been
     * de-activated because the underlying Bloomberg request returns an exception when it is used.
     */
    public IntradayTickRequestBuilder includeNonPlottable() {
        if (true) { //
            throw new UnsupportedOperationException("This override is not available");
        } else {
            this.includeNonPlottable = true;
            return this;
        }
    }

    /**
     * Include the exchange code where this tick originated. Review QR<GO> for more information.
     */
    public IntradayTickRequestBuilder includeExchangeCodes() {
        this.includeExchangeCodes = true;
        return this;
    }

    /**
     * Include the broker code of the trade. The broker code for Canadian, Finnish, Mexican, Philippine, and Swedish
     * equities only. The Market Maker Lookup screen, MMTK<GO>, displays further information on market makers and their
     * corresponding codes.
     */
    public IntradayTickRequestBuilder includeBrokerCodes() {
        this.includeBrokerCodes = true;
        return this;
    }

    /**
     * Include the Reporting Party Side (RPS) transaction codes. The following values appear:
     * <ul>
     * <li>B: A customer transaction where the dealer purchases securities from the customer.
     * <li>S: A customer transaction where the dealer sells securities to the customer.
     * <li>D: An inter-dealer transaction (always from the sell side).
     * </ul>
     */
    public IntradayTickRequestBuilder includeRpsCodes() {
        this.includeRpsCodes = true;
        return this;
    }

    /**
     * Include bank or market identifier code. The BIC, or Bank Identifier Code, as a 4-character unique identifier for
     * each bank that executed and reported the OTC trade, as required by MiFID. BICs are assigned and maintained by
     * SWIFT (Society for Worldwide Interbank Financial Telecommunication). The MIC is the Market Identifier Code, and
     * this indicates the venue on which the trade was executed.
     */
    public IntradayTickRequestBuilder includeBicMicCodes() {
        this.includeBicMicCodes = true;
        return this;
    }

    @Override
    protected void buildRequest(Request request) {
        super.buildRequest(request);
        request.append("eventTypes", eventType);
        request.set("includeConditionCodes", includeConditionCodes);
        request.set("includeExchangeCodes", includeExchangeCodes);
        request.set("includeBrokerCodes", includeBrokerCodes);
        request.set("includeRpsCodes", includeRpsCodes);
        request.set("includeBicMicCodes", includeBicMicCodes);
    }

    @Override
    public BloombergRequestType getRequestType() {
        return BloombergRequestType.INTRADAY_TICK;
    }

    @Override
    public ResultParser<IntradayTickData> getResultParser() {
        return new IntradayTickResultParser(ticker);
    }

    /**
     * Defines the field to be returned for historical intraday tick requests.
     */
    public enum IntradayTickEventType {

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
         * MID_PRICE only applies to the LSE. The mid price is equal to the sum of the best bid price and the best offer
         * price divided by two, and rounded up to be consistent with the relevant price format.
         */
        MID_PRICE,
        /**
         * Automatic trade for London Sets stocks.
         */
        AT_TRADE,
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
