/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package assylias.jbloomberg;

import assylias.jbloomberg.IntradayBarRequestBuilder;
import assylias.jbloomberg.DefaultBloombergSession;
import assylias.jbloomberg.DefaultBloombergSession.BloombergRequest;
import assylias.jbloomberg.DefaultBloombergSession.BloombergService;
import com.bloomberglp.blpapi.Request;
import com.bloomberglp.blpapi.Session;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.joda.time.DateTime;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Yann Le Tallec
 */
public class IntradayBarRequestBuilderNGTest {

    @Test(groups="unit",expectedExceptions = NullPointerException.class)
    public void testConstructor_NullTicker() {
        new IntradayBarRequestBuilder(null, new DateTime(), new DateTime());
    }

    @Test(groups="unit",expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*empty.*")
    public void testConstructor_EmptyTicker() {
        new IntradayBarRequestBuilder("", new DateTime(), new DateTime());
    }

    @Test(groups="unit",expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = ".*null.*")
    public void testConstructor_NullType() {
        new IntradayBarRequestBuilder("ABC", null, new DateTime(), new DateTime());
    }

    @Test(groups="unit",expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = ".*start.*")
    public void testConstructor_NullStartDate() {
        new IntradayBarRequestBuilder("ABC", null, new DateTime());
    }

    @Test(groups="unit",expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = ".*end.*")
    public void testConstructor_NullEndDate() {
        new IntradayBarRequestBuilder("ABC", new DateTime(), null);
    }

    @Test(groups="unit",expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*date.*")
    public void testConstructor_EndBeforeStart() {
        new IntradayBarRequestBuilder("ABC", new DateTime(), new DateTime().minusDays(1));
    }

    @Test(groups="unit",expectedExceptions = IllegalArgumentException.class)
    public void testInvalidPeriod_LessThan1() {
        IntradayBarRequestBuilder builder = new IntradayBarRequestBuilder("ABC", new DateTime(), new DateTime());
        builder.period(0);
    }

    @Test(groups="unit",expectedExceptions = IllegalArgumentException.class)
    public void testInvalidPeriod_MoreThan1440() {
        IntradayBarRequestBuilder builder = new IntradayBarRequestBuilder("ABC", new DateTime(), new DateTime());
        builder.period(1441);
    }

    @Test(groups="unit")
    public void testInvalidPeriod_OK() {
        IntradayBarRequestBuilder builder = new IntradayBarRequestBuilder("ABC", new DateTime(), new DateTime());
        builder.period(1);
        builder.period(1440);
    }

    @Test(groups="unit")
    public void testConstructor_AllOk() {
        new IntradayBarRequestBuilder("ABC", IntradayBarRequestBuilder.EventType.TRADE, new DateTime(), new DateTime());
    }

    @Test(groups="unit")
    public void testServiceType() {
        assertEquals(new IntradayBarRequestBuilder("ABC", new DateTime(), new DateTime()).getServiceType(),
                DefaultBloombergSession.BloombergService.REFERENCE_DATA);
    }

    @Test(groups="unit")
    public void testRequestType() {
        assertEquals(new IntradayBarRequestBuilder("ABC", new DateTime(), new DateTime()).getRequestType(),
                DefaultBloombergSession.BloombergRequest.INTRADAY_BAR);
    }
}
