/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Test(groups = "requires-bloomberg")
public class ReferenceResultParserTest {

    private DefaultBloombergSession session = null;
    private static final int TIMEOUT = 10;

    @BeforeClass
    public void beforeClass() throws BloombergException {
        session = new DefaultBloombergSession();
        session.start();
    }

    @AfterClass
    public void afterClass() throws BloombergException {
        if (session != null) {
            session.stop();
        }
    }

    @Test(expectedExceptions = BloombergException.class, expectedExceptionsMessageRegExp = ".*[Rr]equest.*")
    @SuppressWarnings("unchecked")
    public void testParse_NoTickerErrorResponse() throws Exception {
        ReferenceRequestBuilder rrb = new ReferenceRequestBuilder("ABC", "ABC");
        //Remove the tickers to provoke a ResponseError
        Field f = rrb.getClass().getDeclaredField("tickers");
        f.setAccessible(true);
        Collection<String> tickers = (Collection<String>) f.get(rrb);
        tickers.clear();
        try {
            session.submit(rrb).get(TIMEOUT, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            throw (Exception) e.getCause();
        }
    }

    @Test
    public void testParse_OneInvalidSecurity() throws Exception {
        ReferenceRequestBuilder rrb = new ReferenceRequestBuilder("XXX", "PX_LAST");
        ReferenceData data = session.submit(rrb).get(TIMEOUT, TimeUnit.SECONDS);
        assertTrue(data.hasErrors());
        assertTrue(data.getSecurityErrors().contains("XXX"));
    }

    @Test
    public void testParse_OneInvalidField() throws Exception {
        ReferenceRequestBuilder rrb = new ReferenceRequestBuilder("IBM US Equity", "XXX");
        ReferenceData data = session.submit(rrb).get(TIMEOUT, TimeUnit.SECONDS);
        assertTrue(data.hasErrors());
        assertTrue(data.getFieldErrors().contains("XXX"));
    }

    @Test
    public void testParse_OneSecurityOneFieldOk() throws Exception {
        ReferenceRequestBuilder rrb = new ReferenceRequestBuilder("IBM US Equity", "PX_LAST");
        ReferenceData data = session.submit(rrb).get(TIMEOUT, TimeUnit.SECONDS);
        assertFalse(data.isEmpty());
    }

    @Test
    public void testParse_TwoSecuritiesTwoFieldsOk() throws Exception {
        RequestBuilder<ReferenceData> rrb = new ReferenceRequestBuilder(Arrays.asList("IBM US Equity", "SIE GY Equity"),
                Arrays.asList("PX_LAST", "CRNCY_ADJ_MKT_CAP"));
        ReferenceData data = session.submit(rrb).get(60, TimeUnit.SECONDS);
        assertFalse(data.isEmpty());
        assertTrue(data.forSecurity("IBM US Equity").forField("PX_LAST").asDouble() > 0);
    }

    @Test
    public void testParse_TwoSecuritiesTwoFieldsWithOverride() throws Exception {
        ReferenceRequestBuilder rrb = new ReferenceRequestBuilder(Arrays.asList("IBM US Equity", "SIE GY Equity"),
                Arrays.asList("PX_LAST", "CRNCY_ADJ_MKT_CAP"))
                .addOverride("EQY_FUND_CRNCY", "USD");
        ReferenceData data = session.submit(rrb).get(TIMEOUT, TimeUnit.SECONDS);
        assertFalse(data.isEmpty());
    }

    @Test
    public void testParse_OtherFieldTypes() throws Exception {
        ReferenceRequestBuilder rrb = new ReferenceRequestBuilder("SIE GY Equity",
                Arrays.asList("CUR_EMPLOYEES", "CUR_NUM_EMPLOYEES_AS_PER_DT", "EQY_CONSOLIDATED"));
        ReferenceData data = session.submit(rrb).get(TIMEOUT, TimeUnit.SECONDS);
        assertFalse(data.isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testParse_BulkData() throws Exception {
        ReferenceRequestBuilder rrb = new ReferenceRequestBuilder("SIE GY Equity","TOP_20_HOLDERS_PUBLIC_FILINGS");
        ReferenceData data = session.submit(rrb).get(15, TimeUnit.MINUTES);
        assertFalse(data.isEmpty());
    }
}