/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

@Test(groups = "unit")
public class SubscriptionBuilderTest {

    @Test(expectedExceptions = NullPointerException.class)
    public void testAddListener_Null() {
        new SubscriptionBuilder().addListener(null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testAddSecurities_NullList() {
        new SubscriptionBuilder().addSecurities((List<String>) null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testAddSecurities_ListContainsNull() {
        String s = null;
        new SubscriptionBuilder().addSecurities(Arrays.asList(s));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAddSecurities_ListContainsEmpty() {
        new SubscriptionBuilder().addSecurities(Arrays.asList(""));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testAddSecurity_NullString() {
        new SubscriptionBuilder().addSecurity((String) null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAddSecurity_EmptyString() {
        new SubscriptionBuilder().addSecurity("");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testAddFields_NullList() {
        new SubscriptionBuilder().addFields((List<RealtimeField>) null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testAddFields_ListContainsNull() {
        RealtimeField r = null;
        new SubscriptionBuilder().addFields(Arrays.asList(r));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testAddField_Null() {
        new SubscriptionBuilder().addField((RealtimeField) null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testThrottle_Negative() {
        new SubscriptionBuilder().throttle(-1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testThrottle_TooSmall() {
        new SubscriptionBuilder().throttle(0.001);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testThrottle_TooBig() {
        new SubscriptionBuilder().throttle(86401);
    }

    @Test
    public void testService() {
        assertEquals(new SubscriptionBuilder().getServiceType(), BloombergServiceType.MARKET_DATA);
    }

    @Test
    public void testAddSecurities_1() {
        List<String> list = new ArrayList<>();
        list.add("abc");
        list.add("def");
        SubscriptionBuilder sb = new SubscriptionBuilder().addSecurity("abc").addSecurity("def");
        assertEquals(sb.getSecurities(), list);
    }

    @Test
    public void testAddSecurities_2() {
        List<String> list = new ArrayList<>();
        list.add("abc");
        list.add("def");
        SubscriptionBuilder sb = new SubscriptionBuilder().addSecurities(list);
        assertEquals(sb.getSecurities(), list);
    }

    @Test
    public void testAddSecurities_Duplicate() {
        List<String> list = new ArrayList<>();
        list.add("abc");
        list.add("def");
        list.add("def");
        SubscriptionBuilder sb = new SubscriptionBuilder().addSecurities(list);
        assertTrue(sb.getSecurities().containsAll(list));
        assertEquals(sb.getSecurities().size(), 2); //duplicate removed
    }

    @Test
    public void testAddFields_1() {
        List<RealtimeField> list = new ArrayList<>();
        list.add(RealtimeField.ASK);
        list.add(RealtimeField.BID);
        SubscriptionBuilder sb = new SubscriptionBuilder().addField(RealtimeField.ASK).addField(RealtimeField.BID);
        assertTrue(sb.getFieldsAsString().containsAll(Arrays.asList("ASK", "BID")));
        assertEquals(sb.getFields().size(), 2);
    }

    @Test
    public void testAddFields_2() {
        List<RealtimeField> list = new ArrayList<>();
        list.add(RealtimeField.ASK);
        list.add(RealtimeField.BID);
        SubscriptionBuilder sb = new SubscriptionBuilder().addFields(list);
        assertTrue(sb.getFieldsAsString().containsAll(Arrays.asList("ASK", "BID")));
        assertEquals(sb.getFields().size(), 2);
    }

    @Test
    public void testAddFields_Duplicate() {
        List<RealtimeField> list = new ArrayList<>();
        list.add(RealtimeField.ASK);
        list.add(RealtimeField.BID);
        list.add(RealtimeField.BID);
        SubscriptionBuilder sb = new SubscriptionBuilder().addFields(list);
        assertTrue(sb.getFieldsAsString().containsAll(Arrays.asList("ASK", "BID")));
        assertEquals(sb.getFields().size(), 2);
    }

    @Test
    public void testThrottle_1() {
        SubscriptionBuilder sb = new SubscriptionBuilder().throttle(1);
        assertEquals(sb.getThrottle(), 1d);
    }

    @Test
    public void testThrottle_2() {
        SubscriptionBuilder sb = new SubscriptionBuilder();
        assertEquals(sb.getThrottle(), 0d);
    }

    @Test
    public void testAddListener() {
        DataChangeListener lst1 = e -> {};
        DataChangeListener lst2 = e -> {};
        SubscriptionBuilder sb = new SubscriptionBuilder().addListener(lst1).addListener(lst1).addListener(lst2);
        assertEquals(sb.getListeners().size(), 2);
    }

    @Test
    public void testAddErrorListener() {
        SubscriptionErrorListener lst1 = e -> {};
        SubscriptionErrorListener lst2 = e -> {};
        DataChangeListener lst3 = e -> {};
        SubscriptionBuilder sb = new SubscriptionBuilder().onError(lst1).onError(lst1).onError(lst2).addListener(lst3);
        assertEquals(sb.getListeners().size(), 1);
        assertSame(sb.getErrorListener(), lst2);
    }
}
