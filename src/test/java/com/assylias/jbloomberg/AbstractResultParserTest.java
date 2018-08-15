/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.testng.Assert.assertTrue;

@Test(groups="unit")
public class AbstractResultParserTest {

    private AbstractResultParser parser;

    @BeforeMethod
    public void beforeMethod() {
        parser = new StubResultParser<>(() -> null);
    }

    @Test(expectedExceptions = TimeoutException.class)
    public void testGet_notReady() throws Exception {
        parser.getResult(1, TimeUnit.MILLISECONDS);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testNoMoreMessages_Twice() {
        parser.noMoreMessages();
        parser.noMoreMessages();
    }

    @Test
    public void testGet_NoMoreMessages() throws Exception {
        parser.noMoreMessages();
        parser.getResult();
    }

    @Test
    public void testGet_Twice() throws Exception {
        parser.noMoreMessages();
        RequestResult result1 = parser.getResult();
        RequestResult result2 = parser.getResult();
        assertTrue(result1 == result2);
    }

    @Test
    public void testGet_NoMoreMessages_WithTimeOut() throws Exception {
        parser.noMoreMessages();
        parser.getResult(20, TimeUnit.MILLISECONDS);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testAdd_afterNoMoreMessages() {
        parser.noMoreMessages();
        parser.addMessage(null);
    }
}
