/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package assylias.jbloomberg;

import com.bloomberglp.blpapi.Message;
import java.util.ArrayList;
import java.util.List;

class MockMessageList {
    private final String[] messages;

    public MockMessageList(String... messages) {
        this.messages = messages;
    }

    public List<Message> getList() {
        List<Message> list = new ArrayList<>();
        for (final String s : messages) {
            Message msg = new MockMessage().setMessageType(s).setToString(s);
            list.add(msg);
        }
        return list;
    }
}
