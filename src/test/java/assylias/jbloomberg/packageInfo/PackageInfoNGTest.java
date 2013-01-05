/*
 * Copyright 2012 Yann Le Tallec.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import assylias.jbloomberg.RequestResult;
import assylias.jbloomberg.SubscriptionBuilder;
import java.util.Map;
import org.joda.time.DateTime;
import org.testng.annotations.Test;

/**
 *
 * @author Yann Le Tallec
 */
public class PackageInfoNGTest {

    @Test(groups="unit")
    public void test_HistoricalExample() throws Exception {
        BloombergSession session = new DefaultBloombergSession();
        session.start();
        RequestBuilder hrb = new HistoricalRequestBuilder("SPX Index",
                "PX_LAST", DateTime.now().minusDays(7),
                DateTime.now())
                .fill(HistoricalRequestBuilder.Fill.NIL_VALUE)
                .days(HistoricalRequestBuilder.Days.ALL_CALENDAR_DAYS);
        RequestResult result = session.submit(hrb).get();
        Map<DateTime, Object> data = result.forSecurity("SPX Index").forField("PX_LAST").get();
        for (Map.Entry<DateTime, Object> e : data.entrySet()) {
            DateTime dt = e.getKey();
            double price = (Double) e.getValue();
            System.out.println("[" + dt + "] " + price);
        }
        session.stop();
    }

    @Test(groups="unit")
    public void test_SubscriptionExample() throws InterruptedException, BloombergException {
        BloombergSession session = new DefaultBloombergSession();
        session.start();
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
        session.stop();
    }
}
