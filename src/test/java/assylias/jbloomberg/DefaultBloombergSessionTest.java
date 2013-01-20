/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package assylias.jbloomberg;

import com.bloomberglp.blpapi.Session;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import mockit.Mocked;
import mockit.Verifications;
import org.joda.time.DateTime;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Yann Le Tallec
 */
public class DefaultBloombergSessionTest {

    @BeforeClass
    public void beforeClass() {
    }

    @Test(groups = "unit", expectedExceptions = {BloombergException.class},
    expectedExceptionsMessageRegExp = ".*bbcom.*")
    public void testStart_noBbComm() throws Exception {
        final DefaultBloombergSession session = new DefaultBloombergSession();
        new MockBloombergUtils(false);
        session.start();
    }

    @Test(groups = "unit", expectedExceptions = IllegalStateException.class,
    expectedExceptionsMessageRegExp = ".*start.*")
    public void testStart_calledTwiceOpensOneSession(@Mocked final Session s) throws Exception {
        final DefaultBloombergSession session = new DefaultBloombergSession();
        new MockBloombergUtils(true);
        session.start();
        session.start();
        new Verifications() {
            {
                s.startAsync();
                maxTimes = 1;
            }
        };
    }

    @Test(groups = "unit")
    public void testStart_allGoodMock(@Mocked final Session s) throws Exception {
        new MockBloombergUtils(true);

        final DefaultBloombergSession session = new DefaultBloombergSession();
        session.start();
        new Verifications() {
            {
                s.startAsync();
                times = 1;
            }
        };
    }

    @Test(groups = "requires-bloomberg")
    public void testStart_allGood() throws Exception {
        final DefaultBloombergSession session = new DefaultBloombergSession();
        session.start();
        session.stop();
    }

    @Test(groups = "unit")
    public void testClose_doesNothingIfSessionNotStarted(@Mocked final Session s) throws Exception {
        final DefaultBloombergSession session = new DefaultBloombergSession();
        session.stop();

        new Verifications() {
            {
                s.stop();
                times = 0;
            }
        };
    }

    @Test(groups = "unit", expectedExceptions = NullPointerException.class,
    expectedExceptionsMessageRegExp = ".*request.*")
    public void testSubmit_NullRequest() throws Exception {
        DefaultBloombergSession session = new DefaultBloombergSession();
        session.submit(null);
    }

    @Test(groups = "unit", expectedExceptions = IllegalStateException.class,
    expectedExceptionsMessageRegExp = ".*session.*")
    public void testSubmit_SessionNotStarted() throws Exception {
        MockRequestBuilder mock = new MockRequestBuilder().serviceType(BloombergServiceType.PAGE_DATA);

        DefaultBloombergSession session = new DefaultBloombergSession();
        session.submit(mock.getMockInstance()).get(2, TimeUnit.SECONDS);
    }

    @Test(groups = "unit", expectedExceptions = BloombergException.class)
    public void testSubmit_SessionThrows() throws Exception {
        new MockBloombergUtils(true);
        new MockSession();

        DefaultBloombergSession session = new DefaultBloombergSession();
        session.start();
    }

    @Test(groups = "unit", expectedExceptions = BloombergException.class)
    public void testSubmit_ServiceThrows() throws Exception {
        new MockBloombergUtils(true);
        MockRequestBuilder request = new MockRequestBuilder().serviceType(BloombergServiceType.PAGE_DATA);
        new MockSession().simulateStartAsyncOk();

        DefaultBloombergSession session = new DefaultBloombergSession();
        session.start();

        try {
            session.submit(request.getMockInstance()).get(2, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            throw (Exception) e.getCause();
        }
    }

    @Test(groups = "unit", expectedExceptions = CancellationException.class)
    public void testSubmit_RequestCancelled() throws Exception {
        new MockBloombergUtils(true);
        MockRequestBuilder request = new MockRequestBuilder().serviceType(BloombergServiceType.PAGE_DATA);

        DefaultBloombergSession session = new DefaultBloombergSession();
        session.start();

        try {
            final Future<?> f = session.submit(request.getMockInstance());
            new Thread(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(100);
                        f.cancel(true);
                    } catch (Exception ignore) {
                    }
                }
            }).start();
            f.get(2, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            throw (Exception) e.getCause();
        }
    }

    @Test(groups = "requires-bloomberg")
    public void testOpenService_allGood() throws Exception {
        final DefaultBloombergSession session = new DefaultBloombergSession();
        RequestBuilder request = new HistoricalRequestBuilder("a", "a", new DateTime(), new DateTime());
        session.start();
        session.submit(request).get(2, TimeUnit.SECONDS);
        session.stop();
    }

    @Test(groups = "requires-bloomberg")
    public void testSubscribe() throws Exception {
        final DefaultBloombergSession session = new DefaultBloombergSession();
        session.start();
        session.subscribe(new SubscriptionBuilder().addSecurity("EUR Curncy").addField(RealtimeField.BID));
        Thread.sleep(3000);
        session.stop();
    }

    @Test(groups = "requires-bloomberg")
    public void testSubscribeWhileLongSubmit() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultBloombergSession session1 = new DefaultBloombergSession();
        final DefaultBloombergSession session2 = new DefaultBloombergSession();
        session1.start();
        session2.start();
        session1.subscribe(new SubscriptionBuilder()
                .addSecurity("ESA Index")
                .addSecurity("EUR Curncy")
                .addSecurity("GBP Curncy")
                .addField(RealtimeField.BID_SIZE)
                .addField(RealtimeField.BID)
                .addField(RealtimeField.ASK_SIZE)
                .addField(RealtimeField.ASK)
                .addListener(new DataChangeListener() {

            @Override
            public void dataChanged(DataChangeEvent e) {
                latch.countDown();
            }
        }));

        latch.await(); //wait until the real time data starts coming in
        Future<RequestResult> future = session2.submit(new IntradayBarRequestBuilder("ESA Index", DateTime.now().minusDays(6), DateTime.now()));
        RequestResult result = future.get();
        session1.stop();
        session2.stop();
    }
}
