/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.Map;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

public class HistoricalRequestBuilderTest {

    private static final LocalDate NOW = LocalDate.now();

    @Test(groups="unit",expectedExceptions = NullPointerException.class)
    public void testConstructor_NullTickers() {
        new HistoricalRequestBuilder((Collection<String>) null, Arrays.asList("a"), NOW, NOW);
    }

    @Test(groups="unit",expectedExceptions = NullPointerException.class)
    public void testConstructor_TickersContainsNull() {
        new HistoricalRequestBuilder(Arrays.asList((String) null), Arrays.asList("a"), NOW, NOW);
    }

    @Test(groups="unit",expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*empty.*")
    public void testConstructor_EmptyTickers() {
        new HistoricalRequestBuilder(Collections.<String>emptyList(), Arrays.asList("a"), NOW, NOW);
    }

    @Test(groups="unit",expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*empty\\sstrings.*")
    public void testConstructor_TickersContainsEmptyString() {
        new HistoricalRequestBuilder(Arrays.asList(""), Arrays.asList("a"), NOW, NOW);
    }

    @Test(groups="unit",expectedExceptions = NullPointerException.class)
    public void testConstructor_NullFields() {
        new HistoricalRequestBuilder(Arrays.asList("a"), (Collection<String>) null, NOW, NOW);
    }

    @Test(groups="unit",expectedExceptions = NullPointerException.class)
    public void testConstructor_FieldsContainsNull() {
        new HistoricalRequestBuilder(Arrays.asList("a"), Arrays.asList((String) null), NOW, NOW);
    }

    @Test(groups="unit",expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*empty.*")
    public void testConstructor_EmptyFields() {
        new HistoricalRequestBuilder(Arrays.asList("a"), Collections.<String>emptyList(), NOW, NOW);
    }

    @Test(groups="unit",expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*empty\\sstrings.*")
    public void testConstructor_FieldsContainsEmptyString() {
        new HistoricalRequestBuilder(Arrays.asList("a"), Arrays.asList(""), NOW, NOW);
    }

    @Test(groups="unit",expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = ".*start.*")
    public void testConstructor_NullStartDate() {
        new HistoricalRequestBuilder(Arrays.asList("a"), Arrays.asList("a"), null, NOW);
    }

    @Test(groups="unit",expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = ".*end.*")
    public void testConstructor_NullEndDate() {
        new HistoricalRequestBuilder(Arrays.asList("a"), Arrays.asList("a"), NOW, null);
    }

    @Test(groups="unit",expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*date.*")
    public void testConstructor_EndBeforeStart() {
        new HistoricalRequestBuilder(Arrays.asList("a"), Arrays.asList("a"), NOW, NOW.minusDays(1));
    }

    @Test(groups="unit",expectedExceptions = IllegalArgumentException.class)
    public void testZeroPoints() {
        HistoricalRequestBuilder builder = new HistoricalRequestBuilder("IBM US Equity", "PX_LAST", NOW, NOW);
        builder.maxPoints(0);
    }

    @Test(groups="unit",expectedExceptions = IllegalArgumentException.class)
    public void testPoints_Negative() {
        HistoricalRequestBuilder builder = new HistoricalRequestBuilder("IBM US Equity", "PX_LAST", NOW, NOW);
        builder.maxPoints(-1);
    }

    @Test(groups="unit",expectedExceptions = NullPointerException.class)
    public void testPeriod_Null() {
        HistoricalRequestBuilder builder = new HistoricalRequestBuilder("IBM US Equity", "PX_LAST", NOW, NOW);
        builder.period(null);
    }

    @Test(groups="unit",expectedExceptions = NullPointerException.class)
    public void testPeriodicityAdjustment_Null() {
        HistoricalRequestBuilder builder = new HistoricalRequestBuilder("IBM US Equity", "PX_LAST", NOW, NOW);
        builder.periodicityAdjustment(null);
    }

    @Test(groups="unit")
    public void testConstructor_AllOk() {
        new HistoricalRequestBuilder("IBM US Equity", "PX_LAST", NOW, NOW);
    }

    @Test(groups="unit")
    public void testConstructor_AllOk_WithOverrides() {
        HistoricalRequestBuilder builder = new HistoricalRequestBuilder("IBM US Equity", "PX_LAST", NOW, NOW);
        builder.adjustAbnormalDistributions()
                .adjustDefault()
                .adjustNormalDistributions()
                .adjustSplits()
                .currency(Currency.getInstance("USD"))
                .days(HistoricalRequestBuilder.Days.ACTIVE_DAYS_ONLY)
                .fill(HistoricalRequestBuilder.Fill.NIL_VALUE)
                .maxPoints(20)
                .addOverride("TIME_ZONE_OVERRIDE", "22")
                .period(HistoricalRequestBuilder.Period.DAILY)
                .periodicityAdjustment(HistoricalRequestBuilder.PeriodicityAdjustment.ACTUAL);
    }

    @Test(groups="unit")
    public void testServiceType() {
        assertEquals(new HistoricalRequestBuilder("ABC", "DEF", NOW, NOW).getServiceType(),
                BloombergServiceType.REFERENCE_DATA);
    }

    @Test(groups="unit")
    public void testRequestType() {
        assertEquals(new HistoricalRequestBuilder("ABC", "DEF", NOW, NOW).getRequestType(),
                BloombergRequestType.HISTORICAL_DATA);
    }

    @Test(groups="requires-bloomberg")
    public void catchAll() throws Exception {
        HistoricalRequestBuilder builder = new HistoricalRequestBuilder("IBM US Equity", "CRNCY_ADJ_CURR_EV", NOW.minusWeeks(1), NOW)
                .currency(Currency.getInstance("USD"))
                .maxPoints(1)
                .addOverride("EQY_FUND_CRNCY", "EUR");
        BloombergSession session = new DefaultBloombergSession();
        try {
          session.start();
          Map<LocalDate, TypedObject> data = session.submit(builder).get().forSecurity("IBM US Equity").forField("CRNCY_ADJ_CURR_EV").get();
          assertEquals(data.size(), 1);
        } finally {
          session.stop();
        }
    }
}