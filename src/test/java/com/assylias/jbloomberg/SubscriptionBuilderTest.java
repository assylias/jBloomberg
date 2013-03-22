/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.assylias.jbloomberg.SubscriptionBuilder;
import com.assylias.jbloomberg.DataChangeEvent;
import com.assylias.jbloomberg.RealtimeField;
import com.assylias.jbloomberg.DataChangeListener;
import com.assylias.jbloomberg.BloombergServiceType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

public class SubscriptionBuilderTest {

    @Test(groups = "unit", expectedExceptions = NullPointerException.class)
    public void testAddListener_Null() {
        new SubscriptionBuilder().addListener(null);
    }

    @Test(groups = "unit", expectedExceptions = NullPointerException.class)
    public void testAddSecurities_NullList() {
        new SubscriptionBuilder().addSecurities((List<String>) null);
    }

    @Test(groups = "unit", expectedExceptions = NullPointerException.class)
    public void testAddSecurities_ListContainsNull() {
        String s = null;
        new SubscriptionBuilder().addSecurities(Arrays.asList(s));
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testAddSecurities_ListContainsEmpty() {
        new SubscriptionBuilder().addSecurities(Arrays.asList(""));
    }

    @Test(groups = "unit", expectedExceptions = NullPointerException.class)
    public void testAddSecurity_NullString() {
        new SubscriptionBuilder().addSecurity((String) null);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testAddSecurity_EmptyString() {
        new SubscriptionBuilder().addSecurity("");
    }

    @Test(groups = "unit", expectedExceptions = NullPointerException.class)
    public void testAddFields_NullList() {
        new SubscriptionBuilder().addFields((List<RealtimeField>) null);
    }

    @Test(groups = "unit", expectedExceptions = NullPointerException.class)
    public void testAddFields_ListContainsNull() {
        RealtimeField r = null;
        new SubscriptionBuilder().addFields(Arrays.asList(r));
    }

    @Test(groups = "unit", expectedExceptions = NullPointerException.class)
    public void testAddField_Null() {
        new SubscriptionBuilder().addField((RealtimeField) null);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testThrottle_Negative() {
        new SubscriptionBuilder().throttle(-1);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testThrottle_TooSmall() {
        new SubscriptionBuilder().throttle(0.001);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testThrottle_TooBig() {
        new SubscriptionBuilder().throttle(86401);
    }

    @Test(groups = "unit")
    public void testService() {
        assertEquals(new SubscriptionBuilder().getServiceType(), BloombergServiceType.MARKET_DATA);
    }

    @Test(groups = "unit")
    public void testAddSecurities_1() {
        List<String> list = new ArrayList<>();
        list.add("abc");
        list.add("def");
        SubscriptionBuilder sb = new SubscriptionBuilder().addSecurity("abc").addSecurity("def");
        assertEquals(sb.getSecurities(), list);
    }

    @Test(groups = "unit")
    public void testAddSecurities_2() {
        List<String> list = new ArrayList<>();
        list.add("abc");
        list.add("def");
        SubscriptionBuilder sb = new SubscriptionBuilder().addSecurities(list);
        assertEquals(sb.getSecurities(), list);
    }

    @Test(groups = "unit")
    public void testAddSecurities_Duplicate() {
        List<String> list = new ArrayList<>();
        list.add("abc");
        list.add("def");
        list.add("def");
        SubscriptionBuilder sb = new SubscriptionBuilder().addSecurities(list);
        assertTrue(sb.getSecurities().containsAll(list));
        assertEquals(sb.getSecurities().size(), 2); //duplicate removed
    }

    @Test(groups = "unit")
    public void testAddFields_1() {
        List<RealtimeField> list = new ArrayList<>();
        list.add(RealtimeField.ASK);
        list.add(RealtimeField.BID);
        SubscriptionBuilder sb = new SubscriptionBuilder().addField(RealtimeField.ASK).addField(RealtimeField.BID);
        assertTrue(sb.getFieldsAsString().containsAll(Arrays.asList("ASK", "BID")));
        assertEquals(sb.getFields().size(), 2);
    }

    @Test(groups = "unit")
    public void testAddFields_2() {
        List<RealtimeField> list = new ArrayList<>();
        list.add(RealtimeField.ASK);
        list.add(RealtimeField.BID);
        SubscriptionBuilder sb = new SubscriptionBuilder().addFields(list);
        assertTrue(sb.getFieldsAsString().containsAll(Arrays.asList("ASK", "BID")));
        assertEquals(sb.getFields().size(), 2);
    }

    @Test(groups = "unit")
    public void testAddFields_Duplicate() {
        List<RealtimeField> list = new ArrayList<>();
        list.add(RealtimeField.ASK);
        list.add(RealtimeField.BID);
        list.add(RealtimeField.BID);
        SubscriptionBuilder sb = new SubscriptionBuilder().addFields(list);
        assertTrue(sb.getFieldsAsString().containsAll(Arrays.asList("ASK", "BID")));
        assertEquals(sb.getFields().size(), 2);
    }

    @Test(groups = "unit")
    public void testThrottle_1() {
        SubscriptionBuilder sb = new SubscriptionBuilder().throttle(1);
        assertEquals(sb.getThrottle(), 1d);
    }

    @Test(groups = "unit")
    public void testThrottle_2() {
        SubscriptionBuilder sb = new SubscriptionBuilder();
        assertEquals(sb.getThrottle(), 0d);
    }

    @Test(groups = "unit")
    public void testAddListener() {
        DataChangeListener lst1 = new DataChangeListener() {public void dataChanged(DataChangeEvent e) {}};
        DataChangeListener lst2 = new DataChangeListener() {public void dataChanged(DataChangeEvent e) {}};
        SubscriptionBuilder sb = new SubscriptionBuilder().addListener(lst1).addListener(lst1).addListener(lst2);
        assertEquals(sb.getListeners().size(), 2);
    }
}
