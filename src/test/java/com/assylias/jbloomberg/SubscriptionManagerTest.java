/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.CorrelationID;
import com.bloomberglp.blpapi.Session;
import com.bloomberglp.blpapi.Subscription;
import com.bloomberglp.blpapi.SubscriptionList;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

@Test(groups = "unit")
public class SubscriptionManagerTest {

    private SubscriptionManager sm;
    private Subscriptions subscriptions;
    private BlockingQueue<Data> queue;
    private CountDownLatch latch;
    private AtomicInteger countEvent;
    private EventsManager eventsManager;
    private DefaultBloombergSession session;

    @BeforeClass
    public void beforeClass() {
    }

    @BeforeMethod
    public void beforeMethod() {
        queue = new LinkedBlockingQueue<>();
        eventsManager = new ConcurrentConflatedEventsManager();
        sm = new SubscriptionManager(queue, eventsManager);
        countEvent = new AtomicInteger();
        subscriptions = new Subscriptions();
        Sessions.mockStartedSession();
        Sessions.resetCounter();
        new MockUp<Session>() {
            @Mock
            public void subscribe(SubscriptionList list) {
                for (Subscription s : list) {
                    subscriptions.add(s);
                }
            }

            @Mock
            public void resubscribe(SubscriptionList list) {
                for (Subscription s : list) {
                    subscriptions.replace(s);
                }
            }
        };
        session = new DefaultBloombergSession();
        sm.start(session);
    }

    @AfterMethod
    public void afterMethod() {
        sm.stop(session);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void startNull() {
        SubscriptionManager sm = new SubscriptionManager(queue, eventsManager);
        sm.start(null);
    }

    @Test(expectedExceptions=IllegalStateException.class)
    public void stopNull() {
        sm.stop(new DefaultBloombergSession());
    }

    @Test
    public void testSubscribe_OneSecurityOneFieldThrottle() throws IOException {
        sm.subscribe(new SubscriptionBuilder().addSecurity("ABC").addField(RealtimeField.BID).throttle(5));

        assertTrue(subscriptions.getTickers().contains("ABC"));
        assertTrue(subscriptions.getFields("ABC").contains(RealtimeField.BID));
        assertEquals(subscriptions.getThrottle("ABC"), 5d);
        assertEquals(subscriptions.getSubscriptionsReceived(), 1);
        assertEquals(subscriptions.getReSubscriptionsReceived(), 0);
    }

    @Test
    public void testSubscribe_TwoSecuritiesTwoFieldsNoThrottle() throws IOException {
        List<String> tickers = Arrays.asList("ABC", "DEF");
        List<RealtimeField> fields = Arrays.asList(RealtimeField.ASK, RealtimeField.BID);
        sm.subscribe(new SubscriptionBuilder().addSecurities(tickers).addFields(fields));

        assertSameContent(subscriptions.getTickers(), tickers);
        assertSameContent(subscriptions.getFields("ABC"), fields);
        assertSameContent(subscriptions.getFields("DEF"), fields);
        assertEquals(subscriptions.getThrottle("ABC"), 0d);
        assertEquals(subscriptions.getThrottle("DEF"), 0d);
        assertEquals(subscriptions.getSubscriptionsReceived(), 2);
        assertEquals(subscriptions.getReSubscriptionsReceived(), 0);
    }

    @Test
    public void testSubscribe_DuplicateSecuritySameFields() throws IOException {
        sm.subscribe(new SubscriptionBuilder().addSecurity("ABC").addField(RealtimeField.ASK));
        sm.subscribe(new SubscriptionBuilder().addSecurity("ABC").addField(RealtimeField.ASK));
        assertSameContent(subscriptions.getTickers(), Arrays.asList("ABC"));
        assertSameContent(subscriptions.getFields("ABC"), Arrays.asList(RealtimeField.ASK));
        assertEquals(subscriptions.getSubscriptionsReceived(), 1);
        assertEquals(subscriptions.getReSubscriptionsReceived(), 1);
    }

    @Test
    public void testReSubscribe_AddFields() throws IOException {
        sm.subscribe(new SubscriptionBuilder().addSecurity("ABC").addField(RealtimeField.ASK));
        sm.subscribe(new SubscriptionBuilder().addSecurity("ABC").addField(RealtimeField.BID));
        assertSameContent(subscriptions.getTickers(), Arrays.asList("ABC"));
        assertSameContent(subscriptions.getFields("ABC"), Arrays.asList(RealtimeField.ASK, RealtimeField.BID));
        assertEquals(subscriptions.getSubscriptionsReceived(), 1);
        assertEquals(subscriptions.getReSubscriptionsReceived(), 1);
    }

    @Test
    public void testReSubscribe_ModifyThrottle() throws IOException {
        sm.subscribe(new SubscriptionBuilder().addSecurity("ABC").addField(RealtimeField.ASK));
        assertEquals(subscriptions.getThrottle("ABC"), 0d);
        sm.subscribe(new SubscriptionBuilder().addSecurity("ABC").throttle(1));
        assertEquals(subscriptions.getThrottle("ABC"), 1d);
        sm.subscribe(new SubscriptionBuilder().addSecurity("ABC"));
        assertEquals(subscriptions.getThrottle("ABC"), 0d);
        assertEquals(subscriptions.getSubscriptionsReceived(), 1);
        assertEquals(subscriptions.getReSubscriptionsReceived(), 2);
    }

    @Test
    public void testReSubscribe_ModifyConvertTimestampsToUTC() throws IOException {
        sm.subscribe(new SubscriptionBuilder().addSecurity("ABC").addField(RealtimeField.ASK));
        assertFalse(subscriptions.isConvertTimestampsToUTC("ABC"));
        sm.subscribe(new SubscriptionBuilder().addSecurity("ABC").convertTimestampsToUTC());
        assertTrue(subscriptions.isConvertTimestampsToUTC("ABC"));
        sm.subscribe(new SubscriptionBuilder().addSecurity("ABC"));
        assertFalse(subscriptions.isConvertTimestampsToUTC("ABC"));
        assertEquals(subscriptions.getSubscriptionsReceived(), 1);
        assertEquals(subscriptions.getReSubscriptionsReceived(), 2);
    }

    @Test
    public void testDispatch_1() throws Exception {
        Sessions.mockStartedSession();
        DataChangeListener lst = getListener(1);
        sm.subscribe(new SubscriptionBuilder().addSecurity("ABC").addField(RealtimeField.ASK).addListener(lst));
        queue.add(new Data(new CorrelationID(0), "ASK", 123));
        assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
        assertEquals(countEvent.get(), 0);
    }

    @Test
    public void testDispatch_2() throws Exception {
        Sessions.mockStartedSession();
        DataChangeListener lst = getListener(2);
        sm.subscribe(new SubscriptionBuilder().addSecurity("ABC").addField(RealtimeField.ASK).addListener(lst));
        queue.add(new Data(new CorrelationID(0), "ASK", 123));
        queue.add(new Data(new CorrelationID(0), "ASK", 453));
        assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
        assertEquals(countEvent.get(), 0);
    }

    @Test
    public void testDispatch_FieldNotSubscribed() throws Exception {
        Sessions.mockStartedSession();
        DataChangeListener lst = getListener(1);
        sm.subscribe(new SubscriptionBuilder().addSecurity("ABC").addField(RealtimeField.ASK).addListener(lst));
        queue.add(new Data(new CorrelationID(0), "OTHER", 123));
        assertFalse(latch.await(100, TimeUnit.MILLISECONDS));
        assertNotEquals(countEvent.get(), 0);
    }

    @Test
    public void testDispatch_SecurityNotSubscribed() throws Exception {
        Sessions.mockStartedSession();
        DataChangeListener lst = getListener(1);
        sm.subscribe(new SubscriptionBuilder().addSecurity("ABC").addField(RealtimeField.ASK).addListener(lst));
        queue.add(new Data(new CorrelationID(1), "ASK", 123));
        assertFalse(latch.await(100, TimeUnit.MILLISECONDS));
        assertNotEquals(countEvent.get(), 0);
    }

    @Test
    public void testDispatch_EventNoChange() throws Exception {
        Sessions.mockStartedSession();
        DataChangeListener lst = getListener(1);
        sm.subscribe(new SubscriptionBuilder().addSecurity("ABC").addField(RealtimeField.ASK).addListener(lst));
        queue.add(new Data(new CorrelationID(0), "ASK", 123));
        queue.add(new Data(new CorrelationID(0), "ASK", 123));
        assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
        assertEquals(countEvent.get(), 0);
    }

    @Test
    public void test1Listener2Securities_AddListeners() throws Exception {
        assertEquals(new CorrelationID(0), new CorrelationID(0)); //necessary for the test to pass
        Sessions.mockStartedSession();
        final AtomicInteger expectedInvocations = new AtomicInteger();
        new MockUp<ConcurrentConflatedEventsManager>() {
            @Mock
            public void addEventListener(String ticker, CorrelationID id, RealtimeField field, DataChangeListener lst) {
                if ((ticker.equals("ABC") && id.value() == 0) || (ticker.equals("DEF") && id.value() == 1)) {
                    expectedInvocations.incrementAndGet();
                } else {
                    fail("Unexpected invocation of addEventListener: " + ticker + ", " + id);
                }
            }
        };
        sm.subscribe(new SubscriptionBuilder().addSecurity("ABC").addSecurity("DEF").addField(RealtimeField.ASK).addListener(getListener(1)));
        assertEquals(expectedInvocations.get(), 2);
    }

    @Test
    public void test1Listener2Securities_FireEvents() throws Exception {
        Sessions.mockStartedSession();
        final CountDownLatch latch1 = new CountDownLatch(1);
        final CountDownLatch latch2 = new CountDownLatch(1);
        DataChangeListener lst = new DataChangeListener() {

            @Override
            public void dataChanged(DataChangeEvent e) {
                if (e.getSource().equals("ABC") && e.getNewValue().asInt() == 123) {
                    latch1.countDown();
                } else if (e.getSource().equals("DEF") && e.getNewValue().asInt() == 456) {
                    latch2.countDown();
                } else {
                    fail("Unexpected event: " + e);
                }
            }
        };
        sm.subscribe(new SubscriptionBuilder().addSecurity("ABC").addSecurity("DEF").addField(RealtimeField.ASK).addListener(lst));
        queue.add(new Data(new CorrelationID(0), "ASK", 123));
        queue.add(new Data(new CorrelationID(1), "ASK", 456));
        assertTrue(latch1.await(10000, TimeUnit.MILLISECONDS));
        assertTrue(latch2.await(10000, TimeUnit.MILLISECONDS));
    }

    private static <T> void assertSameContent(Collection<T> expected, Collection<T> actual) {
        if (!expected.containsAll(actual) || !actual.containsAll(expected)) {
            assertEquals(actual, expected); //just for the error message
        }
    }

    @Test
    public void testListener() {
        DataChangeListener lst = getListener(1);
        assertFalse(latch.getCount() == 0);
        lst.dataChanged(null);
        assertTrue(latch.getCount() == 0);

        lst = getListener(1);
        assertFalse(latch.getCount() == 0);
        lst.dataChanged(null);
        assertTrue(latch.getCount() == 0);
        try {
            lst.dataChanged(null);
            fail("Should have failed");
        } catch (IllegalStateException e) {
        }
    }

    private DataChangeListener getListener(final int i) {
        latch = new CountDownLatch(i);
        countEvent.set(i);
        return new DataChangeListener() {
            @Override
            public void dataChanged(DataChangeEvent e) {
                if (countEvent.decrementAndGet() < 0) {
                    throw new IllegalStateException("latch already at 0");
                }
                latch.countDown();
            }
        };
    }
}
class Sessions {
    static AtomicInteger counter = new AtomicInteger(0);

    static void mockStartedSession() {
        mockSession(true);
    }

    static void mockNotStartedSession() {
        mockSession(false);
    }

    static private void mockSession(final boolean isStarted) {
        new MockUp<DefaultBloombergSession>() {

            @Mock
            CorrelationID getNextCorrelationId() {
                return new CorrelationID(counter.getAndIncrement());
            }
        };
    }

    static void resetCounter() {
        counter.set(0);
    }
}

/**
 *
 * a sort of stub for the SubscriptionList class to keep track of what has been sent.
 */
class Subscriptions {

    Map<String, Subscription> subscriptions = new HashMap<>();
    int subs = 0;
    int resubs = 0;

    int getSubscriptionsReceived() {
        return subs;
    }

    int getReSubscriptionsReceived() {
        return resubs;
    }

    void add(Subscription s) {
        subscriptions.put(getTicker(s), s);
        subs++;
    }

    void replace(Subscription s) {
        subscriptions.remove(getTicker(s));
        subscriptions.put(getTicker(s), s);
        resubs++;
    }

    void remove(String ticker) {
        subscriptions.remove(ticker);
    }

    List<String> getTickers() {
        return new ArrayList<>(subscriptions.keySet());
    }

    List<RealtimeField> getFields(String ticker) {
        List<RealtimeField> fields = new ArrayList<>();
        if (subscriptions.containsKey(ticker)) {
            Subscription s = subscriptions.get(ticker);
            String str = s.subscriptionString();
            String[] s0 = str.split("fields=");
            String[] s1 = s0[1].split("&");
            String[] s2 = s1[0].split(",");
            for (String field : s2) {
                fields.add(RealtimeField.valueOf(field));
            }
        }
        return fields;
    }

    double getThrottle(String ticker) {
        if (subscriptions.containsKey(ticker)) {
            String[] s = subscriptions.get(ticker).subscriptionString().split("interval=");
            return s.length >= 2 ? Double.parseDouble(s[1].split(" ")[0]) : 0;
        } else {
            return 0;
        }
    }

    boolean isConvertTimestampsToUTC(String ticker) {
        if (subscriptions.containsKey(ticker)) {
            return subscriptions.get(ticker).subscriptionString().contains("useGMT");
        } else {
            return false;
        }
    }

    Object getCorrelationId(String ticker) {
        CorrelationID id = subscriptions.get(ticker).correlationID();
        return id.isObject() ? id.object() : id.value();
    }

    static String getTicker(Subscription s) {
        return s.subscriptionString().split("\\?")[0];
    }
}
