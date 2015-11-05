/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import java.time.OffsetDateTime;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

public class AbstractIntradayRequestBuilderTest {

    private static final OffsetDateTime NOW = OffsetDateTime.now();

    @Test(groups = "unit", expectedExceptions = NullPointerException.class)
    public void testConstructor_NullTicker() {
        new Impl(null, NOW, NOW);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class,
    expectedExceptionsMessageRegExp = ".*empty.*")
    public void testConstructor_EmptyTicker() {
        new Impl("", NOW, NOW);
    }

    @Test(groups = "unit", expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = ".*null.*")
    public void testConstructor_NullType() {
        new Impl("ABC", null, NOW, NOW);
    }

    @Test(groups = "unit", expectedExceptions = NullPointerException.class,
    expectedExceptionsMessageRegExp = ".*start.*")
    public void testConstructor_NullStartDate() {
        new Impl("ABC", null, NOW);
    }

    @Test(groups = "unit", expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = ".*end.*")
    public void testConstructor_NullEndDate() {
        new Impl("ABC", NOW, null);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class,
    expectedExceptionsMessageRegExp = ".*date.*")
    public void testConstructor_EndBeforeStart() {
        new Impl("ABC", NOW, NOW.minusDays(1));
    }

    @Test(groups = "unit")
    public void testConstructor_AllOk() {
        new Impl("ABC", "TRADE", NOW, NOW);
    }

    @Test(groups = "unit")
    public void testServiceType() {
        assertEquals(new IntradayBarRequestBuilder("ABC", NOW, NOW).getServiceType(),
                BloombergServiceType.REFERENCE_DATA);
    }

    private static class Impl extends AbstractIntradayRequestBuilder {
        public Impl(String ticker, OffsetDateTime startDateTime, OffsetDateTime endDateTime) {
            this(ticker, "TRADE", startDateTime, endDateTime);
        }
        public Impl(String ticker, String eventType, OffsetDateTime startDateTime, OffsetDateTime endDateTime) {
            super(ticker, eventType, startDateTime, endDateTime);
        }
        @Override
        public BloombergRequestType getRequestType() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public ResultParser getResultParser() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
