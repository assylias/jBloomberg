/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.CorrelationID;
import com.bloomberglp.blpapi.Event;
import com.bloomberglp.blpapi.Message;
import com.bloomberglp.blpapi.Session;
import mockit.Mocked;
import mockit.Verifications;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertTrue;

@Test(groups = "unit")
public class BloombergEventHandlerTest {

    private CountDownLatch latch;

    @Test
    public void testProcessEvent_SessionStarted() throws Exception {
        latch = new CountDownLatch(1);
        final BloombergEventHandler handler = new BloombergEventHandler(new ArrayBlockingQueue<>(1), x -> {});
        handler.onSessionStarted(new Runnable() {
            @Override
            public void run() {
                latch.countDown();
            }
        });

        new MockSession().simulateStartAsyncOk();
        Session s = new Session(null, handler);
        s.startAsync();

        assertTrue(latch.await(1, TimeUnit.SECONDS));
    }

    @Test
    public void testProcessEvent_Response(@Mocked final ResultParser<?> parser) throws Exception {
        final BloombergEventHandler handler = new BloombergEventHandler(new ArrayBlockingQueue<>(1), x -> {});
        CorrelationID cId = new CorrelationID(1);
        handler.setParser(cId, parser);

        final Message msg1 = new MockMessage().setMessageType("Message 1").setToString("Message 1").setCorrelationID(1);
        final MockEvent evt1 = new MockEvent(Event.EventType.PARTIAL_RESPONSE, Arrays.asList(msg1));
        handler.processEvent(evt1, null);

        //This message is ignored
        final Message msg2 = new MockMessage().setMessageType("Message 2").setToString("Message 2").setCorrelationID(2);
        final MockEvent evt2 = new MockEvent(Event.EventType.PARTIAL_RESPONSE, Arrays.asList(msg2));
        handler.processEvent(evt2, null);

        final Message msg3 = new MockMessage().setMessageType("Message 3").setToString("Message 3").setCorrelationID(1);
        final MockEvent evt3 = new MockEvent(Event.EventType.RESPONSE, Arrays.asList(msg3));
        handler.processEvent(evt3, null);

        new Verifications() {
            {
                parser.addMessage(msg1);
                parser.addMessage(msg3);
                parser.noMoreMessages();
            }
        };
    }
}
