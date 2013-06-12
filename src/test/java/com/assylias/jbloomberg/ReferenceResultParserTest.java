/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ReferenceResultParserTest {

    private DefaultBloombergSession session = null;

    @BeforeClass(groups = "requires-bloomberg")
    public void beforeClass() throws BloombergException {
//        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel","trace");
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
        ReferenceRequestBuilder hrb = new ReferenceRequestBuilder("ABC", "ABC");
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
        ReferenceRequestBuilder hrb = new ReferenceRequestBuilder("XXX", "PX_LAST");
        ReferenceData data = session.submit(hrb).get(2, TimeUnit.SECONDS);
        assertTrue(data.hasErrors());
        assertTrue(data.getSecurityErrors().contains("XXX"));
    }

    @Test(groups = "requires-bloomberg")
    public void testParse_OneInvalidField() throws Exception {
        ReferenceRequestBuilder hrb = new ReferenceRequestBuilder("IBM US Equity", "XXX");
        ReferenceData data = session.submit(hrb).get(2, TimeUnit.SECONDS);
        assertTrue(data.hasErrors());
        assertTrue(data.getFieldErrors().contains("XXX"));
    }

    @Test(groups = "requires-bloomberg")
    public void testParse_OneSecurityOneFieldOk() throws Exception {
        ReferenceRequestBuilder hrb = new ReferenceRequestBuilder("IBM US Equity", "PX_LAST");
        ReferenceData data = session.submit(hrb).get(2, TimeUnit.SECONDS);
        assertFalse(data.isEmpty());
    }

    @Test(groups = "requires-bloomberg")
    public void testParse_TwoSecuritiesTwoFieldsOk() throws Exception {
        RequestBuilder<ReferenceData> hrb = new ReferenceRequestBuilder(Arrays.asList("IBM US Equity", "SIE GY Equity"),
                Arrays.asList("PX_LAST", "CRNCY_ADJ_MKT_CAP"));
        ReferenceData data = session.submit(hrb).get(60, TimeUnit.SECONDS);
        assertFalse(data.isEmpty());
        assertTrue(data.forSecurity("IBM US Equity").forField("PX_LAST").asDouble() > 0);
    }

    @Test(groups = "requires-bloomberg")
    public void testParse_TwoSecuritiesTwoFieldsWithOverride() throws Exception {
        ReferenceRequestBuilder hrb = new ReferenceRequestBuilder(Arrays.asList("IBM US Equity", "SIE GY Equity"),
                Arrays.asList("PX_LAST", "CRNCY_ADJ_MKT_CAP"))
                .addOverride("EQY_FUND_CRNCY", "USD");
        ReferenceData data = session.submit(hrb).get(2, TimeUnit.SECONDS);
        assertFalse(data.isEmpty());
    }

    @Test(groups = "requires-bloomberg")
    public void testParse_OtherFieldTypes() throws Exception {
        ReferenceRequestBuilder hrb = new ReferenceRequestBuilder("SIE GY Equity",
                Arrays.asList("CUR_EMPLOYEES", "CUR_NUM_EMPLOYEES_AS_PER_DT", "EQY_CONSOLIDATED"));
        ReferenceData data = session.submit(hrb).get(5, TimeUnit.SECONDS);
        assertFalse(data.isEmpty());
    }

    @Test(groups = "requires-bloomberg")
    @SuppressWarnings("unchecked")
    public void testParse_BulkData() throws Exception {
        ReferenceRequestBuilder hrb = new ReferenceRequestBuilder("SIE GY Equity","TOP_20_HOLDERS_PUBLIC_FILINGS");
        ReferenceData data = session.submit(hrb).get(15, TimeUnit.MINUTES);
        assertFalse(data.isEmpty());
        List<TypedObject> list = data.forSecurity("SIE GY Equity").forField("TOP_20_HOLDERS_PUBLIC_FILINGS").asList();
        System.out.println(list);
    }
}