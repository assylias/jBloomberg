/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.CorrelationID;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.*;

@Test(groups="unit", singleThreaded = true) //single threaded to make sure no other test adds keys at the same time
public class EventsKeyTest {


    public void testUnique() {
        EventsKey key1 = EventsKey.of(new CorrelationID(0), RealtimeField.ASK);
        EventsKey key2 = EventsKey.of(new CorrelationID(0), RealtimeField.ASK);
        assertEquals(key1, key2);
        assertSame(key1, key2);
    }

    public void testHash() {
        EventsKey key1 = EventsKey.of(new CorrelationID(0), RealtimeField.ASK);
        EventsKey key2 = EventsKey.of(new CorrelationID(0), RealtimeField.ASK);
        assertEquals(key1.hashCode(), key2.hashCode());
        assertNotEquals(key1.hashCode(), 0);
    }

    public void testNotEquals() {
        EventsKey key1 = EventsKey.of(new CorrelationID(1), RealtimeField.ASK);
        EventsKey key2 = EventsKey.of(new CorrelationID(2), RealtimeField.ASK);
        assertNotEquals(key1, key2);
        assertNotEquals(key1.hashCode(), key2.hashCode());
    }

    public void testConcurrent() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch start = new CountDownLatch(1);
        for (int i = 0; i < 1000; i++) {
            executor.submit(getRunnable(start));
        }
        int countKeysStart = countKeys();
        start.countDown();
        executor.shutdown();
        if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
            fail("Tasks did not complete");
        }
        int countKeysEnd = countKeys();
        assertEquals(countKeysEnd, countKeysStart + 2000);
    }

    private static int countKeys() throws Exception {
        Field f = EventsKey.class.getDeclaredField("keys");
        f.setAccessible(true);
        return ((Map) f.get(null)).size();
    }

    private static Runnable getRunnable(CountDownLatch start) {
        return () -> {
            try {
                start.await();
            } catch (InterruptedException ex) {}
            for (int i = 10_000; i < 11_000; i++) {
                EventsKey key1 = EventsKey.of(new CorrelationID(i), RealtimeField.ASK);
                EventsKey key2 = EventsKey.of(new CorrelationID(i), RealtimeField.BID);
                assertNotEquals(key1, key2); //pretend we are doing something
            }
        };
    }
}
