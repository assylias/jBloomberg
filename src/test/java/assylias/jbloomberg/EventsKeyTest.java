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
package assylias.jbloomberg;

import com.bloomberglp.blpapi.CorrelationID;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Yann Le Tallec
 */
@Test(groups="unit")
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
        start.countDown();
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);
        Field f = EventsKey.class.getDeclaredField("keys");
        f.setAccessible(true);
        assertEquals(((Map) f.get(EventsKey.of(new CorrelationID(0), RealtimeField.ASK))).size(), 2000);
    }

    private static Runnable getRunnable(final CountDownLatch start) {
        return new Runnable() {

            @Override
            public void run() {
                try {
                    start.await();
                } catch (InterruptedException ex) {}
                for (int i = 0; i < 1000; i++) {
                    EventsKey key1 = EventsKey.of(new CorrelationID(i), RealtimeField.ASK);
                    EventsKey key2 = EventsKey.of(new CorrelationID(i), RealtimeField.BID);
                    assertNotEquals(key1, key2); //pretend we are doing something
                }
            }
        };
    }
}
