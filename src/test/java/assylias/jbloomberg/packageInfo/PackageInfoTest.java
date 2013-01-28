/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package assylias.jbloomberg.packageInfo;

import assylias.jbloomberg.BloombergException;
import assylias.jbloomberg.BloombergSession;
import assylias.jbloomberg.DataChangeEvent;
import assylias.jbloomberg.DataChangeListener;
import assylias.jbloomberg.DefaultBloombergSession;
import assylias.jbloomberg.HistoricalRequestBuilder;
import assylias.jbloomberg.RealtimeField;
import assylias.jbloomberg.RequestBuilder;
import assylias.jbloomberg.HistoricalData;
import assylias.jbloomberg.IntradayBarData;
import assylias.jbloomberg.IntradayBarField;
import assylias.jbloomberg.IntradayBarRequestBuilder;
import assylias.jbloomberg.IntradayTickData;
import assylias.jbloomberg.IntradayTickField;
import assylias.jbloomberg.IntradayTickRequestBuilder;
import assylias.jbloomberg.ReferenceData;
import assylias.jbloomberg.ReferenceRequestBuilder;
import assylias.jbloomberg.RequestResult;
import assylias.jbloomberg.SubscriptionBuilder;
import com.google.common.collect.Multimap;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.joda.time.DateTime;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups="unit")
public class PackageInfoTest {

    private static final BloombergSession session = new DefaultBloombergSession();

    @BeforeClass
    public void beforeClass() throws Exception {
        session.start();
    }

    @AfterClass
    public void afterClass() throws Exception {
        session.stop();
    }

    @Test
    public void test_ReferenceExample() throws Exception {
        RequestBuilder<ReferenceData> hrb = new ReferenceRequestBuilder(
                "SPX Index", "CRNCY_ADJ_PX_LAST")
                .addOverride("EQY_FUND_CRNCY", "JPY");
        ReferenceData result = session.submit(hrb).get();
        double priceInYen = (double) result.forSecurity("SPX Index").forField("CRNCY_ADJ_PX_LAST");
        System.out.println("SPX in Yen = " + priceInYen);
    }

    @Test
    public void test_HistoricalExample() throws Exception {
        RequestBuilder<HistoricalData> hrb = new HistoricalRequestBuilder("SPX Index",
                "PX_LAST", DateTime.now().minusDays(7),
                DateTime.now())
                .fill(HistoricalRequestBuilder.Fill.NIL_VALUE)
                .days(HistoricalRequestBuilder.Days.ALL_CALENDAR_DAYS);
        HistoricalData result = session.submit(hrb).get();
        Map<DateTime, Object> data = result.forSecurity("SPX Index").forField("PX_LAST").get();
        for (Map.Entry<DateTime, Object> e : data.entrySet()) {
            DateTime dt = e.getKey();
            double price = (Double) e.getValue();
            System.out.println("[" + dt + "] " + price);
        }
    }

    @Test
    public void test_IntradayBarExample() throws Exception {
        RequestBuilder<IntradayBarData> hrb = new IntradayBarRequestBuilder("SPX Index",
                DateTime.now().minusDays(7),
                DateTime.now())
                .adjustSplits()
                .fillInitialBar()
                .period(1, TimeUnit.HOURS);
        IntradayBarData result = session.submit(hrb).get();
        Map<DateTime, Object> data = result.forField(IntradayBarField.CLOSE).get();
        for (Map.Entry<DateTime, Object> e : data.entrySet()) {
            DateTime dt = e.getKey();
            double price = (Double) e.getValue();
            System.out.println("[" + dt + "] " + price);
        }
    }

    @Test
    public void test_IntradayTickExample() throws Exception {
        RequestBuilder<IntradayTickData> hrb = new IntradayTickRequestBuilder("SPX Index",
                DateTime.now().minusHours(2),
                DateTime.now())
                .includeBrokerCodes()
                .includeConditionCodes();
        IntradayTickData result = session.submit(hrb).get();
        Multimap<DateTime, Object> data = result.forField(IntradayTickField.VALUE);
        for (Map.Entry<DateTime, Object> e : data.entries()) {
            DateTime dt = e.getKey();
            double price = (Double) e.getValue();
            System.out.println("[" + dt + "] " + price);
        }
    }

    @Test
    public void test_SubscriptionExample() throws InterruptedException, BloombergException {
        DataChangeListener lst = new DataChangeListener() {
            @Override
            public void dataChanged(DataChangeEvent e) {
                System.out.println(e);
            }
        };
        SubscriptionBuilder builder = new SubscriptionBuilder()
                .addSecurity("ESA Index")
                .addField(RealtimeField.LAST_PRICE)
                .addField(RealtimeField.ASK)
                .addField(RealtimeField.ASK_SIZE)
                .addListener(lst);
        session.subscribe(builder);
        Thread.sleep(3000);
    }
}
