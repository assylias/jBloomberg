/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.CorrelationID;
import com.google.common.collect.ImmutableMap;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

@Test(groups = "unit")
public class EventsManagerTest {

    private EventsManager em;
    private CountDownLatch latch;
    private final AtomicInteger countEvent = new AtomicInteger();
    private volatile DataChangeEvent evt;
    private CorrelationID id;
    private RealtimeField field;
    private String ticker;

    @BeforeMethod
    public void beforeMethod() {
        em = new ConcurrentConflatedEventsManager();
        id = new CorrelationID(0);
        field = RealtimeField.ASK;
        ticker = "TICKER";
    }

    @Test
    public void testFire_FieldNotRegistered() throws Exception {
        DataChangeListener lst = getDataChangeListener(1);
        em.addEventListener(ticker, id, Collections.singleton(field), lst);
        em.fireEvents(id, ImmutableMap.of(RealtimeField.ASK_ALL_SESSION, 1234));
        assertFalse(latch.await(10, TimeUnit.MILLISECONDS));
    }

    @Test
    public void testFire_Ok() throws Exception {
        DataChangeListener lst = getDataChangeListener(1);
        em.addEventListener(ticker, id, Collections.singleton(field), lst);
        em.fireEvents(id, ImmutableMap.of(field, 1234));
        assertTrue(latch.await(10, TimeUnit.MILLISECONDS));
        assertEquals(evt.getDataName(), "ASK");
        assertNull(evt.getOldValue());
        assertEquals(evt.getNewValue().asInt(), 1234);
    }

    @Test
    public void testFire_SameValueTwiceSentOnce() throws Exception {
        DataChangeListener lst = getDataChangeListener(2);
        em.addEventListener(ticker, id, Collections.singleton(field), lst);
        em.fireEvents(id, ImmutableMap.of(field, 1234));
        em.fireEvents(id, ImmutableMap.of(field, 1234));
        assertFalse(latch.await(10, TimeUnit.MILLISECONDS)); //second event not sent to listener
        assertEquals(evt.getDataName(), "ASK");
        assertNull(evt.getOldValue());
        assertEquals(evt.getNewValue().asInt(), 1234);
    }

    //TODO: it seems that the order of events is not preserved which could be an issue in case of two successive
    //data points on the same security
    //The problem is that the current setup does not allow to strongly guarantee the order and the solution is probably
    //to have a single queue but that may prove to be an issue performance wise if some listeners do a lot of work with new data...
    @Test(enabled = false, invocationCount = 20, threadPoolSize = 2)
    public void testFire_2Listeners() throws Exception {
        CountDownLatch latch = new CountDownLatch(4);
        String ticker = "" + new Random().nextDouble();
        List<DataChangeEvent> events = new CopyOnWriteArrayList<>();
        DataChangeListener lst1 = getDataChangeListener(latch, events);
        DataChangeListener lst2 = getDataChangeListener(latch, events);
        em.addEventListener(ticker, id, Collections.singleton(field), lst1);
        em.addEventListener(ticker, id, Collections.singleton(field), lst2);
        em.fireEvents(id, ImmutableMap.of(field, 1));
        em.fireEvents(id, ImmutableMap.of(field, 2));
        assertTrue(latch.await(500, TimeUnit.MILLISECONDS), msg(latch, events));
        assertEquals(events.size(), 4);
        assertEquals(events.get(0).getDataName(), "ASK");
        assertNull(events.get(0).getOldValue(), msg(latch, events));
        assertNotNull(events.get(3).getOldValue(), msg(latch, events));
        assertEquals(events.get(3).getOldValue().asInt(), 1, msg(latch, events));
        assertEquals(events.get(3).getNewValue().asInt(), 2, msg(latch, events));
    }

    private String msg(CountDownLatch latch, List<DataChangeEvent> e) {
      return "latch.count  = " + latch.getCount() + ", evt = " + String.valueOf(e);
    }

    @Test
    public void test2Listeners2Securities() throws Exception {
        latch = new CountDownLatch(2);
        CorrelationID id1 = new CorrelationID(0);
        CorrelationID id2 = new CorrelationID(1);
        em.addEventListener("SEC 1", id1, Collections.singleton(field), new DataChangeListener() {
            @Override
            public void dataChanged(DataChangeEvent e) {
                assertEquals(e.getSource(), "SEC 1");
                assertEquals(e.getDataName(), field.toString());
                assertEquals(e.getOldValue(), null);
                assertEquals(e.getNewValue().asInt(), 123);
                latch.countDown();
            }
        });
        em.addEventListener("SEC 2", id2, Collections.singleton(field), new DataChangeListener() {
            @Override
            public void dataChanged(DataChangeEvent e) {
                assertEquals(e.getSource(), "SEC 2");
                assertEquals(e.getDataName(), field.toString());
                assertEquals(e.getOldValue(), null);
                assertEquals(e.getNewValue().asInt(), 456);
                latch.countDown();
            }
        });
        em.fireEvents(id1, ImmutableMap.of(field, 123));
        em.fireEvents(id2, ImmutableMap.of(field, 456));
        assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
    }

    @Test
    public void test1Listener2Securities() throws Exception {
        final CountDownLatch latch1 = new CountDownLatch(1);
        final CountDownLatch latch2 = new CountDownLatch(1);
        CorrelationID id1 = new CorrelationID(0);
        CorrelationID id2 = new CorrelationID(1);
        DataChangeListener lst = new DataChangeListener() {
            @Override
            public void dataChanged(DataChangeEvent e) {
                if (e.getSource().equals("SEC 1")
                        && e.getDataName().equals(field.toString())
                        && e.getOldValue() == null
                        && e.getNewValue().asInt() == 123) {
                    latch1.countDown();
                } else if (e.getSource().equals("SEC 2")
                        && e.getDataName().equals(field.toString())
                        && e.getOldValue() == null
                        && e.getNewValue().asInt() == 456) {
                    latch2.countDown();
                } else {
                    fail("Unexpected event received: " + e);
                }
            }
        };
        em.addEventListener("SEC 1", id1, Collections.singleton(field), lst);
        em.addEventListener("SEC 2", id2, Collections.singleton(field), lst);
        em.fireEvents(id1, ImmutableMap.of(field, 123));
        em.fireEvents(id2, ImmutableMap.of(field, 456));
        assertTrue(latch1.await(100, TimeUnit.MILLISECONDS));
        assertTrue(latch2.await(100, TimeUnit.MILLISECONDS));
    }

    @Test
    public void testFire_Concurrent() throws Exception {
        final int NUM_EVENTS = 10_000;
        final int NUM_THREADS = 100;
        final int NUM_PER_THREAD = NUM_EVENTS / NUM_THREADS;
        final DataChangeEvent[] events = new DataChangeEvent[NUM_EVENTS];
        latch = new CountDownLatch(NUM_EVENTS);
        countEvent.set(0);
        DataChangeListener lst = new DataChangeListener() {
            @Override
            public void dataChanged(DataChangeEvent e) {
                events[countEvent.getAndIncrement()] = e;
                latch.countDown();
            }
        };
        em.addEventListener(ticker, id, Collections.singleton(field), lst);
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

        for (int i = 0; i < NUM_THREADS; i++) {
            final int start = i * NUM_PER_THREAD;
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    for (int i = start; i < start + NUM_PER_THREAD; i++) {
                        em.fireEvents(id, ImmutableMap.of(field, i));
                    }
                }
            };
            executor.submit(r);
        }
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);

        assertTrue(latch.await(1, TimeUnit.SECONDS), "latch at " + latch.getCount());
        Set<TypedObject> newValues = new HashSet<>();
        Set<TypedObject> oldValues = new HashSet<>();
        for (DataChangeEvent e : events) {
            assertEquals(e.getSource(), ticker);
            assertEquals(e.getDataName(), "ASK");
            newValues.add(e.getNewValue());
            oldValues.add(e.getOldValue());
        }
        assertEquals(newValues.size(), NUM_EVENTS);
        assertEquals(oldValues.size(), NUM_EVENTS); //including null
    }

    @Test
    public void testFire_Performance() throws Exception {
        final int NUM_EVENTS = 1_000_000;
        final int NUM_THREADS = 1;
        final int NUM_PER_THREAD = NUM_EVENTS / NUM_THREADS;
        countEvent.set(0);
        DataChangeListener lst = new DataChangeListener() {
            @Override
            public void dataChanged(DataChangeEvent e) {
                countEvent.getAndIncrement();
            }
        };
        em.addEventListener(ticker, id, Collections.singleton(field), lst);
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

        for (int i = 0; i < NUM_THREADS; i++) {
            final int start = i * NUM_PER_THREAD;
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    for (int i = start; i < start + NUM_PER_THREAD; i++) {
                        em.fireEvents(id, ImmutableMap.of(field, i));
                    }
                }
            };
            executor.submit(r);
        }
        long start = System.nanoTime();
        executor.shutdown();
        executor.awaitTermination(60, TimeUnit.SECONDS);
        long time = (System.nanoTime() - start) / 1_000_000;
    }

    private DataChangeListener getDataChangeListener(final int i) {
        latch = new CountDownLatch(i);
        countEvent.set(i);
        return new DataChangeListener() {
            @Override
            public void dataChanged(DataChangeEvent e) {
                if (countEvent.decrementAndGet() < 0) fail("latch already at 0");
                evt = e;
                latch.countDown();
            }
        };
    }

    private DataChangeListener getDataChangeListener(CountDownLatch latch, List<DataChangeEvent> evt) {
        return e -> {
          if (latch.getCount() == 0) fail("latch already at 0");
          evt.add(e);
          latch.countDown();
        };
    }
}
