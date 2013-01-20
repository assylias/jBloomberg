/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package assylias.jbloomberg;

import assylias.jbloomberg.RequestResult;
import assylias.jbloomberg.ReferenceRequestBuilder;
import assylias.jbloomberg.BloombergException;
import assylias.jbloomberg.DefaultBloombergSession;
import com.bloomberglp.blpapi.Element;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.joda.time.DateTime;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Yann Le Tallec
 */
public class ReferenceResultParserTest {

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
        RequestResult data = session.submit(hrb).get(2, TimeUnit.SECONDS);
        assertTrue(data.hasErrors());
        assertTrue(data.getSecurityErrors().contains("XXX"));
    }

    @Test(groups = "requires-bloomberg")
    public void testParse_OneInvalidField() throws Exception {
        ReferenceRequestBuilder hrb = new ReferenceRequestBuilder("IBM US Equity", "XXX");
        RequestResult data = session.submit(hrb).get(2, TimeUnit.SECONDS);
        assertTrue(data.hasErrors());
        assertTrue(data.getFieldErrors().contains("XXX"));
    }

    @Test(groups = "requires-bloomberg")
    public void testParse_OneSecurityOneFieldOk() throws Exception {
        ReferenceRequestBuilder hrb = new ReferenceRequestBuilder("IBM US Equity", "PX_LAST");
        RequestResult data = session.submit(hrb).get(2, TimeUnit.SECONDS);
        assertFalse(data.isEmpty());
    }

    @Test(groups = "requires-bloomberg")
    public void testParse_TwoSecuritiesTwoFieldsOk() throws Exception {
        ReferenceRequestBuilder hrb = new ReferenceRequestBuilder(Arrays.asList("IBM US Equity", "SIE GY Equity"),
                Arrays.asList("PX_LAST", "CRNCY_ADJ_MKT_CAP"));
        RequestResult data = session.submit(hrb).get(60, TimeUnit.SECONDS);
        assertFalse(data.isEmpty());
        assertTrue((double) data.forSecurity("IBM US Equity").forField("PX_LAST").get().values().iterator().next() > 0);
    }

    @Test(groups = "requires-bloomberg")
    public void testParse_TwoSecuritiesTwoFieldsWithOverride() throws Exception {
        ReferenceRequestBuilder hrb = new ReferenceRequestBuilder(Arrays.asList("IBM US Equity", "SIE GY Equity"),
                Arrays.asList("PX_LAST", "CRNCY_ADJ_MKT_CAP"))
                .addOverride("EQY_FUND_CRNCY", "USD");
        RequestResult data = session.submit(hrb).get(2, TimeUnit.SECONDS);
        assertFalse(data.isEmpty());
    }

    @Test(groups = "requires-bloomberg")
    public void testParse_OtherFieldTypes() throws Exception {
        ReferenceRequestBuilder hrb = new ReferenceRequestBuilder("SIE GY Equity",
                Arrays.asList("CUR_EMPLOYEES", "CUR_NUM_EMPLOYEES_AS_PER_DT", "EQY_CONSOLIDATED"));
        RequestResult data = session.submit(hrb).get(5, TimeUnit.SECONDS);
        assertFalse(data.isEmpty());
    }
}
