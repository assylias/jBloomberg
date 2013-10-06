/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class HistoricalResultParserTest {

    private static final LocalDate NOW = LocalDate.now();
    private DefaultBloombergSession session = null;

    @BeforeClass(groups = "requires-bloomberg")
    public void beforeClass() throws BloombergException {
        session = new DefaultBloombergSession();
        session.start();
    }

    @AfterClass(groups = "requires-bloomberg")
    public void afterClass() throws BloombergException {
        if (session != null) {
            session.stop();
        }
    }

    @Test(groups = "requires-bloomberg", expectedExceptions = BloombergException.class,
    expectedExceptionsMessageRegExp = ".*[Rr]equest.*")
    @SuppressWarnings("unchecked")
    public void testParse_NoTickerErrorResponse() throws Exception {
        HistoricalRequestBuilder hrb = new HistoricalRequestBuilder("ABC", "ABC", NOW, NOW);
        //Remove the tickers to provoke a ResponseError
        Field f = hrb.getClass().getDeclaredField("tickers");
        f.setAccessible(true);
        Collection<String> tickers = (Collection<String>) f.get(hrb);
        tickers.clear();
        try {
            session.submit(hrb).get(2, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            throw (Exception) e.getCause();
        }
    }

    @Test(groups = "requires-bloomberg")
    public void testParse_OneInvalidSecurity() throws Exception {
        HistoricalRequestBuilder hrb = new HistoricalRequestBuilder("XXX", "PX_LAST", NOW, NOW);
        RequestResult data = session.submit(hrb).get(2, TimeUnit.SECONDS);
        assertTrue(data.hasErrors());
    }

    @Test(groups = "requires-bloomberg")
    public void testParse_OneInvalidField() throws Exception {
        HistoricalRequestBuilder hrb = new HistoricalRequestBuilder("IBM US Equity", "XXX", NOW, NOW);
        RequestResult data = session.submit(hrb).get(2, TimeUnit.SECONDS);
        assertTrue(data.hasErrors());
    }

    @Test(groups = "requires-bloomberg")
    public void testParse_OneSecurityOneFieldOk() throws Exception {
        HistoricalRequestBuilder hrb = new HistoricalRequestBuilder("IBM US Equity", "PX_LAST", NOW.minusDays(5), NOW);
        RequestResult data = session.submit(hrb).get(2, TimeUnit.SECONDS);
        assertFalse(data.isEmpty());
    }

    @Test(groups = "requires-bloomberg")
    public void testParse_TwoSecuritiesTwoFieldsOk() throws Exception {
        HistoricalRequestBuilder hrb = new HistoricalRequestBuilder(Arrays.asList("IBM US Equity", "SIE GY Equity"),
                Arrays.asList("PX_LAST"), NOW.minusDays(5), NOW)
                .fill(HistoricalRequestBuilder.Fill.NIL_VALUE).days(HistoricalRequestBuilder.Days.ALL_CALENDAR_DAYS);
        RequestResult data = session.submit(hrb).get(2, TimeUnit.SECONDS);
        assertFalse(data.isEmpty());
    }
}
