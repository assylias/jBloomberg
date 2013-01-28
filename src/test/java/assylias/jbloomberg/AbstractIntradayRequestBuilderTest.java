/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package assylias.jbloomberg;

import org.joda.time.DateTime;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

public class AbstractIntradayRequestBuilderTest {

    @Test(groups = "unit", expectedExceptions = NullPointerException.class)
    public void testConstructor_NullTicker() {
        new Impl(null, new DateTime(), new DateTime());
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class,
    expectedExceptionsMessageRegExp = ".*empty.*")
    public void testConstructor_EmptyTicker() {
        new Impl("", new DateTime(), new DateTime());
    }

    @Test(groups = "unit", expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = ".*null.*")
    public void testConstructor_NullType() {
        new Impl("ABC", null, new DateTime(), new DateTime());
    }

    @Test(groups = "unit", expectedExceptions = NullPointerException.class,
    expectedExceptionsMessageRegExp = ".*start.*")
    public void testConstructor_NullStartDate() {
        new Impl("ABC", null, new DateTime());
    }

    @Test(groups = "unit", expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = ".*end.*")
    public void testConstructor_NullEndDate() {
        new Impl("ABC", new DateTime(), null);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class,
    expectedExceptionsMessageRegExp = ".*date.*")
    public void testConstructor_EndBeforeStart() {
        new Impl("ABC", new DateTime(), new DateTime().minusDays(1));
    }

    @Test(groups = "unit")
    public void testConstructor_AllOk() {
        new Impl("ABC", "TRADE", new DateTime(), new DateTime());
    }

    @Test(groups = "unit")
    public void testServiceType() {
        assertEquals(new IntradayBarRequestBuilder("ABC", new DateTime(), new DateTime()).getServiceType(),
                BloombergServiceType.REFERENCE_DATA);
    }

    private static class Impl extends AbstractIntradayRequestBuilder {
        public Impl(String ticker, DateTime startDateTime, DateTime endDateTime) {
            this(ticker, "TRADE", startDateTime, endDateTime);
        }
        public Impl(String ticker, String eventType, DateTime startDateTime, DateTime endDateTime) {
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
