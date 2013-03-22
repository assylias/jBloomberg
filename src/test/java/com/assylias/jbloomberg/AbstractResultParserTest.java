/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.assylias.jbloomberg.AbstractResultParser;
import com.assylias.jbloomberg.AbstractRequestResult;
import com.assylias.jbloomberg.RequestResult;
import com.bloomberglp.blpapi.Element;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AbstractResultParserTest {

    private AbstractResultParser parser;

    @BeforeMethod(groups="unit")
    public void beforeMethod() {
        parser = new AbstractResultParser () {
            @Override
            protected void parseResponseNoResponseError(Element response) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            protected AbstractRequestResult getRequestResult() {
                return null;
            }
        };
    }

    @Test(groups="unit", expectedExceptions = TimeoutException.class)
    public void testGet_notReady() throws Exception {
        parser.getResult(1, TimeUnit.MILLISECONDS);
    }

    @Test(groups="unit", expectedExceptions = IllegalStateException.class)
    public void testNoMoreMessages_Twice() {
        parser.noMoreMessages();
        parser.noMoreMessages();
    }

    @Test(groups="unit")
    public void testGet_NoMoreMessages() throws Exception {
        parser.noMoreMessages();
        parser.getResult();
    }

    @Test(groups="unit")
    public void testGet_Twice() throws Exception {
        parser.noMoreMessages();
        RequestResult result1 = parser.getResult();
        RequestResult result2 = parser.getResult();
        assertTrue(result1 == result2);
    }

    @Test(groups="unit")
    public void testGet_NoMoreMessages_WithTimeOut() throws Exception {
        parser.noMoreMessages();
        parser.getResult(20, TimeUnit.MILLISECONDS);
    }

    @Test(groups="unit", expectedExceptions = IllegalStateException.class)
    public void testAdd_afterNoMoreMessages() {
        parser.noMoreMessages();
        parser.addMessage(null);
    }
}
