/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

public class IntradayBarRequestBuilderTest {
    private static final LocalDateTime NOW = LocalDateTime.now();

    @Test(groups = "unit")
    public void testRequestType() {
        assertEquals(new IntradayBarRequestBuilder("ABC", NOW, NOW).getRequestType(),
                BloombergRequestType.INTRADAY_BAR);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testInvalidPeriod_LessThan1() {
        IntradayBarRequestBuilder builder = new IntradayBarRequestBuilder("ABC", NOW, NOW);
        builder.period(0, TimeUnit.MINUTES);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testInvalidPeriod_MoreThan1440() {
        IntradayBarRequestBuilder builder = new IntradayBarRequestBuilder("ABC", NOW, NOW);
        builder.period(1441, TimeUnit.MINUTES);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testInvalidPeriod_MoreThan1440_2() {
        IntradayBarRequestBuilder builder = new IntradayBarRequestBuilder("ABC", NOW, NOW);
        builder.period(25, TimeUnit.HOURS);
    }

    @Test(groups = "unit")
    public void testInvalidPeriod_OK() {
        IntradayBarRequestBuilder builder = new IntradayBarRequestBuilder("ABC", NOW, NOW);
        builder.period(1, TimeUnit.MINUTES);
        builder.period(1440, TimeUnit.MINUTES);
        builder.period(24, TimeUnit.HOURS);
        builder.period(1, TimeUnit.DAYS);
        builder.period(60, TimeUnit.SECONDS);
    }
}
