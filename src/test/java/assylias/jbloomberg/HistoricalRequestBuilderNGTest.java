/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package assylias.jbloomberg;

import assylias.jbloomberg.DefaultBloombergSession;
import assylias.jbloomberg.HistoricalRequestBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import org.joda.time.DateTime;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Yann Le Tallec
 */

public class HistoricalRequestBuilderNGTest {

    @Test(groups="unit",expectedExceptions = NullPointerException.class)
    public void testConstructor_NullTickers() {
        new HistoricalRequestBuilder((Collection) null, Arrays.asList("a"), new DateTime(), new DateTime());
    }

    @Test(groups="unit",expectedExceptions = NullPointerException.class)
    public void testConstructor_TickersContainsNull() {
        new HistoricalRequestBuilder(Arrays.<String>asList(null), Arrays.asList("a"), new DateTime(), new DateTime());
    }

    @Test(groups="unit",expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*empty.*")
    public void testConstructor_EmptyTickers() {
        new HistoricalRequestBuilder(Collections.EMPTY_LIST, Arrays.asList("a"), new DateTime(), new DateTime());
    }

    @Test(groups="unit",expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*empty\\sstrings.*")
    public void testConstructor_TickersContainsEmptyString() {
        new HistoricalRequestBuilder(Arrays.asList(""), Arrays.asList("a"), new DateTime(), new DateTime());
    }

    @Test(groups="unit",expectedExceptions = NullPointerException.class)
    public void testConstructor_NullFields() {
        new HistoricalRequestBuilder(Arrays.asList("a"), (Collection) null, new DateTime(), new DateTime());
    }

    @Test(groups="unit",expectedExceptions = NullPointerException.class)
    public void testConstructor_FieldsContainsNull() {
        new HistoricalRequestBuilder(Arrays.asList("a"), Arrays.<String>asList(null), new DateTime(), new DateTime());
    }

    @Test(groups="unit",expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*empty.*")
    public void testConstructor_EmptyFields() {
        new HistoricalRequestBuilder(Arrays.asList("a"), Collections.EMPTY_LIST, new DateTime(), new DateTime());
    }

    @Test(groups="unit",expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*empty\\sstrings.*")
    public void testConstructor_FieldsContainsEmptyString() {
        new HistoricalRequestBuilder(Arrays.asList("a"), Arrays.asList(""), new DateTime(), new DateTime());
    }

    @Test(groups="unit",expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = ".*start.*")
    public void testConstructor_NullStartDate() {
        new HistoricalRequestBuilder(Arrays.asList("a"), Arrays.asList("a"), null, new DateTime());
    }

    @Test(groups="unit",expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = ".*end.*")
    public void testConstructor_NullEndDate() {
        new HistoricalRequestBuilder(Arrays.asList("a"), Arrays.asList("a"), new DateTime(), null);
    }

    @Test(groups="unit",expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*date.*")
    public void testConstructor_EndBeforeStart() {
        new HistoricalRequestBuilder(Arrays.asList("a"), Arrays.asList("a"), new DateTime(), new DateTime().minusDays(1));
    }

    @Test(groups="unit",expectedExceptions = IllegalArgumentException.class)
    public void testZeroPoints() {
        HistoricalRequestBuilder builder = new HistoricalRequestBuilder("IBM US Equity", "PX_LAST", new DateTime(), new DateTime());
        builder.maxPoints(0);
    }

    @Test(groups="unit",expectedExceptions = IllegalArgumentException.class)
    public void testPoints_Negative() {
        HistoricalRequestBuilder builder = new HistoricalRequestBuilder("IBM US Equity", "PX_LAST", new DateTime(), new DateTime());
        builder.maxPoints(-1);
    }

    @Test(groups="unit",expectedExceptions = NullPointerException.class)
    public void testPeriod_Null() {
        HistoricalRequestBuilder builder = new HistoricalRequestBuilder("IBM US Equity", "PX_LAST", new DateTime(), new DateTime());
        builder.period(null);
    }

    @Test(groups="unit",expectedExceptions = NullPointerException.class)
    public void testPeriodicityAdjustment_Null() {
        HistoricalRequestBuilder builder = new HistoricalRequestBuilder("IBM US Equity", "PX_LAST", new DateTime(), new DateTime());
        builder.periodicityAdjusment(null);
    }

    @Test(groups="unit")
    public void testConstructor_AllOk() {
        new HistoricalRequestBuilder("IBM US Equity", "PX_LAST", new DateTime(), new DateTime());
    }

    @Test(groups="unit")
    public void testConstructor_AllOk_WithOverrides() {
        HistoricalRequestBuilder builder = new HistoricalRequestBuilder("IBM US Equity", "PX_LAST", new DateTime(), new DateTime());
        builder.adjustAbnormalDistributions(true)
                .adjustDefault(true)
                .adjustNormalDistributions(true)
                .adjustSplits(true)
                .currency(Currency.getInstance("USD"))
                .days(HistoricalRequestBuilder.Days.ACTIVE_DAYS_ONLY)
                .fill(HistoricalRequestBuilder.Fill.NIL_VALUE)
                .maxPoints(20)
                .period(HistoricalRequestBuilder.Period.DAILY)
                .periodicityAdjusment(HistoricalRequestBuilder.PeriodicityAdjustment.ACTUAL);
    }

    @Test(groups="unit")
    public void testServiceType() {
        assertEquals(new HistoricalRequestBuilder("ABC", "DEF", new DateTime(), new DateTime()).getServiceType(),
                BloombergServiceType.REFERENCE_DATA);
    }

    @Test(groups="unit")
    public void testRequestType() {
        assertEquals(new HistoricalRequestBuilder("ABC", "DEF", new DateTime(), new DateTime()).getRequestType(),
                BloombergRequestType.HISTORICAL_DATA);
    }
}
