/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Test(groups = "requires-bloomberg")
public class IntradayBarResultParserTest {

    private static final OffsetDateTime NOW = OffsetDateTime.now();
    private final String INVALID_SECURITY = "XXX";
    private DefaultBloombergSession session = null;

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

    @Test
    public void testParse_InvalidSecurity() throws Exception {
        IntradayBarRequestBuilder builder = new IntradayBarRequestBuilder(INVALID_SECURITY, NOW.minusDays(5), NOW);
        RequestResult data = session.submit(builder).get(5, TimeUnit.SECONDS);
        assertTrue(data.hasErrors());
        assertFalse(data.getSecurityErrors().isEmpty());
        assertTrue(data.getFieldErrors().isEmpty());
        assertTrue(data.isEmpty());
    }

    @Test
    public void testParse_OK() throws Exception {
        IntradayBarRequestBuilder builder = new IntradayBarRequestBuilder("IBM US Equity", NOW.minusDays(5), NOW);
        builder.adjustAbnormalDistributions()
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
