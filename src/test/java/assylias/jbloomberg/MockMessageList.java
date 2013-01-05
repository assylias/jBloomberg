/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package assylias.jbloomberg;

import com.bloomberglp.blpapi.CorrelationID;
import com.bloomberglp.blpapi.Datetime;
import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Message;
import com.bloomberglp.blpapi.Message.Fragment;
import com.bloomberglp.blpapi.Name;
import com.bloomberglp.blpapi.Service;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Yann Le Tallec
 */
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
