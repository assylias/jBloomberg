/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.CorrelationID;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class EventsManagerTest {

    private EventsManager em;
    private CountDownLatch latch;
    private final AtomicInteger countEvent = new AtomicInteger();
    private volatile DataChangeEvent evt;
    private CorrelationID id;
    private RealtimeField field;
    private String ticker;

    @BeforeMethod(groups = "unit")
    public void beforeMethod() {
        em = new ConcurrentConflatedEventsManager();
        id = new CorrelationID(0);
        field = RealtimeField.ASK;
        ticker = "TICKER";
    }

    @Test(groups = "unit")
    public void testFire_FieldNotRegistered() throws Exception {
        DataChangeListener lst = getDataChangeListener(1);
        em.addEventListener(ticker, id, field, lst);
        em.fireEvent(id, RealtimeField.ASK_ALL_SESSION, 1234);
        assertFalse(latch.await(10, TimeUnit.MILLISECONDS));
    }

    @Test(groups = "unit")
    public void testFire_Ok() throws Exception {
        DataChangeListener lst = getDataChangeListener(1);
        em.addEventListener(ticker, id, field, lst);
        em.fireEvent(id, field, 1234);
        assertTrue(latch.await(10, TimeUnit.MILLISECONDS));
        assertEquals(evt.getDataName(), "ASK");
        assertNull(evt.getOldValue());
        assertEquals(evt.getNewValue(), 1234);
    }

    @Test(groups = "unit")
    public void testFire_SameValueTwiceSentOnce() throws Exception {
        DataChangeListener lst = getDataChangeListener(2);
        em.addEventListener(ticker, id, field, lst);
        em.fireEvent(id, field, 1234);
        em.fireEvent(id, field, 1234);
        assertFalse(latch.await(10, TimeUnit.MILLISECONDS)); //second event not sent to listener
        assertEquals(evt.getDataName(), "ASK");
        assertNull(evt.getOldValue());
        assertEquals(evt.getNewValue(), 1234);
    }

    @Test(groups = "unit")
    public void testFire_2Listeners() throws Exception {
        DataChangeListener lst1 = getDataChangeListener(2);
        DataChangeListener lst2 = getDataChangeListener(2);
        latch = new CountDownLatch(4);
        countEvent.set(4);
        em.addEventListener(ticker, id, field, lst1);
        em.addEventListener(ticker, id, field, lst2);
        em.fireEvent(id, field, 1234);
        em.fireEvent(id, field, 234);
        assertTrue(latch.await(10, TimeUnit.MILLISECONDS));
        assertEquals(evt.getDataName(), "ASK");
        assertEquals(evt.getOldValue(), 1234);
        assertEquals(evt.getNewValue(), 234);
    }

    @Test(groups = "unit")
    public void test2Listeners2Securities() throws Exception {
        latch = new CountDownLatch(2);
        CorrelationID id1 = new CorrelationID(0);
        CorrelationID id2 = new CorrelationID(1);
        em.addEventListener("SEC 1", id1, field, new DataChangeListener() {
            @Override
            public void dataChanged(DataChangeEvent e) {
                assertEquals(e.getSource(), "SEC 1");
                assertEquals(e.getDataName(), field.toString());
                assertEquals(e.getOldValue(), null);
                assertEquals(e.getNewValue(), 123);
                latch.countDown();
            }
        });
        em.addEventListener("SEC 2", id2, field, new DataChangeListener() {
            @Override
            public void dataChanged(DataChangeEvent e) {
                assertEquals(e.getSource(), "SEC 2");
                assertEquals(e.getDataName(), field.toString());
                assertEquals(e.getOldValue(), null);
                assertEquals(e.getNewValue(), 456);
                latch.countDown();
            }
        });
        em.fireEvent(id1, field, 123);
        em.fireEvent(id2, field, 456);
        assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
    }

    @Test(groups = "unit")
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
                        && e.getNewValue().equals(123)) {
                    latch1.countDown();
                } else if (e.getSource().equals("SEC 2")
                        && e.getDataName().equals(field.toString())
                        && e.getOldValue() == null
                        && e.getNewValue().equals(456)) {
                    latch2.countDown();
                } else {
                    fail("Unexpected event received: " + e);
                }
            }
        };
        em.addEventListener("SEC 1", id1, field, lst);
        em.addEventListener("SEC 2", id2, field, lst);
        em.fireEvent(id1, field, 123);
        em.fireEvent(id2, field, 456);
        assertTrue(latch1.await(100, TimeUnit.MILLISECONDS));
        assertTrue(latch2.await(100, TimeUnit.MILLISECONDS));
    }

    @Test(groups = "unit")
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
        em.addEventListener(ticker, id, field, lst);
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

        for (int i = 0; i < NUM_THREADS; i++) {
            final int start = i * NUM_PER_THREAD;
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    for (int i = start; i < start + NUM_PER_THREAD; i++) {
                        em.fireEvent(id, field, i);
                    }
                }
            };
            executor.submit(r);
        }
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);

        assertTrue(latch.await(1, TimeUnit.SECONDS), "latch at " + latch.getCount());
        Set<Integer> newValues = new HashSet<>();
        Set<Integer> oldValues = new HashSet<>();
        for (DataChangeEvent e : events) {
            assertEquals(e.getSource(), ticker);
            assertEquals(e.getDataName(), "ASK");
            newValues.add((Integer) e.getNewValue());
            oldValues.add((Integer) e.getOldValue());
        }
        assertEquals(newValues.size(), NUM_EVENTS);
        assertEquals(oldValues.size(), NUM_EVENTS); //including null
    }

    @Test(groups = "unit")
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
        em.addEventListener(ticker, id, field, lst);
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

        for (int i = 0; i < NUM_THREADS; i++) {
            final int start = i * NUM_PER_THREAD;
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    for (int i = start; i < start + NUM_PER_THREAD; i++) {
                        em.fireEvent(id, field, i);
                    }
                }
            };
            executor.submit(r);
        }
        long start = System.nanoTime();
        executor.shutdown();
        System.out.println("Terminating");
        executor.awaitTermination(60, TimeUnit.SECONDS);
        long time = (System.nanoTime() - start) / 1_000_000;
        System.out.println("Terminated in " + time + " ms with " + countEvent);
    }

    private DataChangeListener getDataChangeListener(final int i) {
        latch = new CountDownLatch(i);
        countEvent.set(i);
        return new DataChangeListener() {
            @Override
            public void dataChanged(DataChangeEvent e) {
                if (countEvent.decrementAndGet() < 0) {
                    throw new IllegalStateException("latch already at 0");
                }
                evt = e;
                latch.countDown();
            }
        };
    }
}
