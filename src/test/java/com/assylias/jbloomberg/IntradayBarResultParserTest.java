/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import java.util.concurrent.TimeUnit;
import org.joda.time.DateTime;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class IntradayBarResultParserTest {

    private final String INVALID_SECURITY = "XXX";
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

    @Test(groups = "requires-bloomberg")
    public void testParse_InvalidSecurity() throws Exception {
        IntradayBarRequestBuilder builder = new IntradayBarRequestBuilder(INVALID_SECURITY, new DateTime().minusDays(5), new DateTime());
        RequestResult data = session.submit(builder).get(5, TimeUnit.SECONDS);
        assertTrue(data.hasErrors());
        assertFalse(data.getSecurityErrors().isEmpty());
        assertTrue(data.getFieldErrors().isEmpty());
        assertTrue(data.isEmpty());
    }

    @Test(groups = "unit", expectedExceptions = UnsupportedOperationException.class)
    public void testParse_CantAddFieldError() throws Exception {
        IntradayBarResultParser parser = new IntradayBarResultParser(INVALID_SECURITY);
        parser.addFieldError("");
    }

    @Test(groups = "requires-bloomberg")
    public void testParse_OK() throws Exception {
        IntradayBarRequestBuilder builder = new IntradayBarRequestBuilder("IBM US Equity", new DateTime().minusDays(5), new DateTime());
        builder.adjustAbnormalDistributions()
                .adjustDefault()
                .adjustNormalDistributions()
                .adjustSplits()
                .fillInitialBar()
                .period(240, TimeUnit.MINUTES);
        RequestResult data = session.submit(builder).get(5, TimeUnit.SECONDS);
        assertFalse(data.hasErrors());
        assertTrue(data.getSecurityErrors().isEmpty());
        assertTrue(data.getFieldErrors().isEmpty());
        assertFalse(data.isEmpty());
    }
}
