/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package assylias.jbloomberg;

import com.bloomberglp.blpapi.Request;
import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.Collection;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.joda.time.DateTime;

/**
 * This class enables to build a historical request while ensuring argument safety. Typically, instead of passing
 * strings arguments (and typos) as with the standard Bloomberg API, the possible options used to override the behaviour
 * of the query have been wrapped in enums or relevant primitive types.
 * <p/>
 * All methods, including the constructors, throw NullPointerException when null arguments are passed in.
 * <p/>
 * Once the request has been built, the HistoricalRequestBuilder can be submitted to a BloombergSession.
 * <p/>
 * <b>This class is not thread safe.</b>
 */
public final class HistoricalRequestBuilder extends AbstractRequestBuilder {

    //Required parameters
    private final Set<String> tickers = new HashSet<>();
    private final Set<String> fields = new HashSet<>();
    private final DateTime startDate;
    private final DateTime endDate;
    //Optional parameters
    private PeriodicityAdjustment periodicityAdjustment = PeriodicityAdjustment.ACTUAL;
    private Period period = Period.DAILY;
    private Currency currency = null;
    private Days days = Days.ACTIVE_DAYS_ONLY;
    private Fill fill = Fill.NIL_VALUE;
    private int points = 0;
    private boolean adjNormal = false;
    private boolean adjAbnormal = false;
    private boolean adjSplit = false;
    private boolean adjDefault = false;

    /**
     * Equivalent to calling
     * <code> new HistoricalRequestBuilder(Arrays.asList(ticker), Arrays.asList(field), startDate, endDate);
     * </code>
     */
    public HistoricalRequestBuilder(String ticker, String field, DateTime startDate, DateTime endDate) {
        this(Arrays.asList(ticker), Arrays.asList(field), startDate, endDate);
    }

    /**
     * Equivalent to calling
     * <code> new HistoricalRequestBuilder(Arrays.asList(ticker), fields, startDate, endDate);
     * </code>
     */
    public HistoricalRequestBuilder(String ticker, List<String> fields, DateTime startDate, DateTime endDate) {
        this(Arrays.asList(ticker), fields, startDate, endDate);
    }

    /**
     * Equivalent to calling
     * <code> new HistoricalRequestBuilder(tickers, Arrays.asList(field), startDate, endDate);
     * </code>
     */
    public HistoricalRequestBuilder(List<String> tickers, String field, DateTime startDate, DateTime endDate) {
        this(tickers, Arrays.asList(field), startDate, endDate);
    }

    /**
     * Creates a HistoricalRequestBuilder with standard options. The Builder can be further customised with the provided
     * methods.
     * <p/>
     * @param tickers   a collection of tickers for which data needs to be retrieved - tickers must be valid Bloomberg
     *                  symbols (for example: IBM US Equity)
     * @param fields    a collection of Bloomberg fields to retrieve for each ticker
     * @param startDate the start of the date range (inclusive) for which to retrieve data
     * @param endDate   the end of the date range (inclusive) for which to retrieve data
     * <p/>
     * @throws NullPointerException     if any of the parameters is null or if the collections contain null items
     * @throws IllegalArgumentException if <ul> <li> any of the collections is empty or contains empty strings <li> the
     *                                  start date is strictly after the end date </ul>
     */
    public HistoricalRequestBuilder(Collection<String> tickers, Collection<String> fields, DateTime startDate, DateTime endDate) {
        this.startDate = Preconditions.checkNotNull(startDate, "The start date must not be null");
        this.endDate = Preconditions.checkNotNull(endDate, "The end date must not be null");
        Preconditions.checkArgument(!startDate.isAfter(endDate), "The start date (%s) must not be after the end date (%s)", startDate, endDate);
        Preconditions.checkArgument(!tickers.isEmpty(), "The list of tickers must not be empty");
        Preconditions.checkArgument(!fields.isEmpty(), "The list of fields must not be empty");
        Preconditions.checkArgument(!tickers.contains(""), "The list of tickers must not contain empty strings");
        Preconditions.checkArgument(!fields.contains(""), "The list of fields must not contain empty strings");

        this.tickers.addAll(tickers);
        this.fields.addAll(fields);
    }

    /**
     * Sets the period and calendar type of the output. To be used in conjunction with Period Selection.
     */
    public HistoricalRequestBuilder periodicityAdjusment(PeriodicityAdjustment periodicityAdjusment) {
        this.periodicityAdjustment = Preconditions.checkNotNull(periodicityAdjusment);
        return this;
    }

    /**
     * @param period Sets the period of the output. To be used in conjunction with Period Adjustment.
     */
    public HistoricalRequestBuilder period(Period period) {
        this.period = Preconditions.checkNotNull(period);
        return this;
    }

    /**
     * @param currency Sets the currency in which the values are returned
     */
    public HistoricalRequestBuilder currency(Currency currency) {
        this.currency = Preconditions.checkNotNull(currency);
        return this;
    }

    /**
     * Sets to include/exclude non trading days where no data was generated.
     */
    public HistoricalRequestBuilder days(Days days) {
        this.days = Preconditions.checkNotNull(days);
        return this;
    }

    /**
     * If data is to be displayed for non trading days what is the data to be returned.
     */
    public HistoricalRequestBuilder fill(Fill fill) {
        this.fill = Preconditions.checkNotNull(fill);
        return this;
    }

    /**
     * @param maxPoints Sets the maximum number of data points to return.
     * <p/>
     * @throws IllegalArgumentException if the number of points is negative
     */
    public HistoricalRequestBuilder maxPoints(int maxPoints) {
        if (maxPoints <= 0) {
            throw new IllegalArgumentException("Maximum number of points must be positive: " + maxPoints);
        }
        this.points = maxPoints;
        return this;
    }

    /**
     * Adjust historical pricing based on the DPDF<GO> BLOOMBERG PROFESSIONAL service function.
     */
    public HistoricalRequestBuilder adjustDefault() {
        this.adjDefault = true;
        return this;
    }

    /**
     * Adjust historical pricing to reflect: Special Cash, Liquidation, Capital Gains, Long-Term Capital Gains,
     * Short-Term Capital Gains, Memorial, Return of Capital, Rights Redemption, Miscellaneous, Return Premium,
     * Preferred Rights Redemption, Proceeds/Rights, Proceeds/Shares, Proceeds/ Warrants.
     */
    public HistoricalRequestBuilder adjustAbnormalDistributions() {
        this.adjAbnormal = true;
        return this;
    }

    /**
     * Adjust historical pricing to reflect: Regular Cash, Interim, 1st Interim, 2nd Interim, 3rd Interim, 4th Interim,
     * 5th Interim, Income, Estimated, Partnership Distribution, Final, Interest on Capital, Distribution, Prorated.
     */
    public HistoricalRequestBuilder adjustNormalDistributions() {
        this.adjNormal = true;
        return this;
    }

    /**
     * Adjust historical pricing and/or volume are adjusted to reflect: Spin-Offs, Stock Splits/Consolidations, Stock
     * Dividend/Bonus, Rights Offerings/ Entitlement.
     */
    public HistoricalRequestBuilder adjustSplits() {
        this.adjSplit = true;
        return this;
    }

    @Override
    public String toString() {
        return "HistoricalQueryBuilder{" + "tickers=" + tickers + ", fields=" + fields + ", startDate=" + startDate + ", endDate=" + endDate + ", periodicityAdjustment=" + periodicityAdjustment + ", period=" + period + ", currency=" + currency + ", days=" + days + ", fill=" + fill + ", points=" + points + ", adjNormal=" + adjNormal + ", adjAbnormal=" + adjAbnormal + ", adjSplit=" + adjSplit + ", adjDefault=" + adjDefault + '}';
    }

    @Override
    public BloombergServiceType getServiceType() {
        return BloombergServiceType.REFERENCE_DATA;
    }

    @Override
    public BloombergRequestType getRequestType() {
        return BloombergRequestType.HISTORICAL_DATA;
    }

    @Override
    protected void buildRequest(Request request) {
        addCollectionToElement(request, tickers, "securities");
        addCollectionToElement(request, fields, "fields");

        request.set("periodicityAdjustment", periodicityAdjustment.toString());
        request.set("periodicitySelection", period.toString());
        if (currency != null) {
            request.set("currency", currency.getCurrencyCode());
        }
        request.set("nonTradingDayFillOption", days.toString());
        request.set("nonTradingDayFillMethod", fill.toString());
        if (points != 0) {
            request.set("maxDataPoints", points);
        }
        request.set("adjustmentNormal", adjNormal);
        request.set("adjustmentAbnormal", adjAbnormal);
        request.set("adjustmentSplit", adjSplit);
        request.set("adjustmentFollowDPDF", adjDefault);
        request.set("startDate", startDate.toString(BB_REQUEST_DATE_FORMATTER));
        request.set("endDate", endDate.toString(BB_REQUEST_DATE_FORMATTER));
    }

    @Override
    public ResultParser getResultParser() {
        return new HistoricalResultParser();
    }

    /**
     * Defines the periodicity adjustment.
     */
    public static enum PeriodicityAdjustment {

        /**
         * These revert to the actual date from today (if the end date is left blank) or from the End Date
         */
        ACTUAL,
        /**
         * For pricing fields, these revert to the last business day of the specified calendar period. Calendar
         * Quarterly (CQ), Calendar Semi-Annually (CS) or Calendar Yearly (CY).
         */
        CALENDAR,
        /**
         * These periods revert to the fiscal period end for the company - Fiscal Quarterly (FQ), Fiscal Semi- Annually
         * (FS) and Fiscal Yearly (FY) only
         */
        FISCAL;
    }

    /**
     * Defines the period used for historical data requests (daily, weekly etc.).
     */
    public static enum Period {

        DAILY,
        WEEKLY,
        MONTHLY,
        QUARTERLY,
        SEMI_ANNUALLY,
        YEARLY;
    }

    /**
     * Defines what days are returned in historical requests (weekdays only, all days etc.).
     */
    public static enum Days {

        /**
         * Include all weekdays (Monday to Friday) in the data set
         * <p/>
         */
        NON_TRADING_WEEKDAYS, //TODO: Confirm if other days are returned in some countries (Russia, Korea, Taiwan, Israel...)
        /**
         * Include all days of the calendar in the data set returned
         */
        ALL_CALENDAR_DAYS,
        /**
         * Include only active days (days where the instrument and field pair updated) in the data set returned
         */
        ACTIVE_DAYS_ONLY;
    }

    /**
     * Defines what values are returned when a field has no value on a specific date.
     */
    public static enum Fill {

        /**
         * Search back and retrieve the previous value available for this security field pair. The search back period is
         * up to one month.
         */
        PREVIOUS_VALUE,
        /**
         * Returns blank for the "value" value within the data element for this field.
         */
        NIL_VALUE;
    }
}
