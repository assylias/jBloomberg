/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.Request;
import mockit.Mocked;
import mockit.Verifications;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;

import static org.testng.Assert.assertEquals;

@Test(groups = "unit")
public class IntradayBarRequestBuilderTest {
    private static final OffsetDateTime NOW = OffsetDateTime.now();

    @Test
    public void testRequestType() {
        assertEquals(new IntradayBarRequestBuilder("ABC", NOW, NOW).getRequestType(),
                BloombergRequestType.INTRADAY_BAR);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidPeriod_LessThan1() {
        IntradayBarRequestBuilder builder = new IntradayBarRequestBuilder("ABC", NOW, NOW);
        builder.period(0, TimeUnit.MINUTES);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidPeriod_MoreThan1440() {
        IntradayBarRequestBuilder builder = new IntradayBarRequestBuilder("ABC", NOW, NOW);
        builder.period(1441, TimeUnit.MINUTES);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidPeriod_MoreThan1440_2() {
        IntradayBarRequestBuilder builder = new IntradayBarRequestBuilder("ABC", NOW, NOW);
        builder.period(25, TimeUnit.HOURS);
    }

    @Test
    public void testInvalidPeriod_OK() {
        IntradayBarRequestBuilder builder = new IntradayBarRequestBuilder("ABC", NOW, NOW);
        builder.period(1, TimeUnit.MINUTES);
        builder.period(1440, TimeUnit.MINUTES);
        builder.period(24, TimeUnit.HOURS);
        builder.period(1, TimeUnit.DAYS);
        builder.period(60, TimeUnit.SECONDS);
    }

    @DataProvider(name = "adjustments") public Object[][] adjustments() {
        return new Object[][] {
            // the operations to apply to the builder   adjNormal   adjAbnormal adjSplit    adjDpdf
            { (UnaryOperator<IntradayBarRequestBuilder>) b ->  b, false, false, false, true },
            { (UnaryOperator<IntradayBarRequestBuilder>) b ->  b.ignorePricingDefaults(), false, false, false, false },
            { (UnaryOperator<IntradayBarRequestBuilder>) b ->  b.adjustNormalDistributions(), true, false, false, false },
            { (UnaryOperator<IntradayBarRequestBuilder>) b ->  b.adjustAbnormalDistributions(), false, true, false, false},
            { (UnaryOperator<IntradayBarRequestBuilder>) b ->  b.adjustSplits(), false, false, true, false},
            { (UnaryOperator<IntradayBarRequestBuilder>) b ->  b.adjustSplits().adjustNormalDistributions().adjustAbnormalDistributions(), true, true, true, false},
        };
    }

    @Mocked Request request;

    @Test(dataProvider = "adjustments")
    public void test_adjustments(UnaryOperator<IntradayBarRequestBuilder> u, boolean adjNormal, boolean adjAbnormal, boolean adjSplit, boolean adjDpdf) {
        u.apply(new IntradayBarRequestBuilder("ABC", NOW, NOW))
                .buildRequest(request);

        new Verifications() {{
            request.set("adjustmentNormal", adjNormal);
            request.set("adjustmentAbnormal", adjAbnormal);
            request.set("adjustmentSplit", adjSplit);
            request.set("adjustmentFollowDPDF", adjDpdf);
        }};
    }
}
