/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.Event;
import com.bloomberglp.blpapi.EventHandler;
import com.bloomberglp.blpapi.Message;
import com.bloomberglp.blpapi.Session;
import com.bloomberglp.blpapi.SessionOptions;
import java.io.IOException;
import java.util.List;
import mockit.Mock;
import mockit.MockUp;

public class MockSession extends MockUp<Session> {

    private EventHandler handler;
    private Event startAsyncEvent;
    private boolean openServiceOk;

    public MockSession simulateStartAsyncOk() {
        List<Message> messages = new MockMessageList("SessionStarted").getList();
        this.startAsyncEvent = new MockEvent(Event.EventType.SESSION_STATUS, messages);
        return this;
    }

    public MockSession simulateSessionStartupFailure() {
        List<Message> messages = new MockMessageList("SessionStartupFailure").getList();
        this.startAsyncEvent = new MockEvent(Event.EventType.SESSION_STATUS, messages);
        return this;
    }

    public MockSession setOpenServiceOk() {
        openServiceOk = true;
        return this;
    }

    @Mock
    public void $init(SessionOptions ignore, EventHandler handler) {
        this.handler = handler;
    }

    @Mock
    public void startAsync() throws IOException {
        if (startAsyncEvent == null) {
            throw new IOException();
        }
        handler.processEvent(startAsyncEvent, this.getMockInstance());
    }

    @Mock
    public boolean openService(String serviceUri) throws IOException {
        if (!openServiceOk) {
            throw new IOException();
        }
        return true;
    }
}
