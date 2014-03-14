/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import java.util.Map;
import org.joda.time.DateTime;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class HistoricalDataTest {

    private HistoricalData data;

    @BeforeMethod(groups = "unit")
    public void beforeMethod() {
        data = new HistoricalData();
    }

    @Test(groups = "unit")
    public void testIsEmpty_Empty() {
        assertTrue(data.isEmpty());
    }

    @Test(groups = "unit")
    public void testIsEmpty_NotEmpty() {
        data.add(new DateTime(), "IBM", "PX LAST", 123.45);
        assertFalse(data.isEmpty());
    }

    @Test(groups = "unit")
    public void testGetData() {
        DateTime date = new DateTime();
        data.add(date, "IBM", "PX LAST", 123);
        TypedObject value = data.forSecurity("IBM").forField("PX LAST").forDate(date);
        assertEquals(123, value.asInt());

        value = data.forSecurity("IBM").forDate(date).forField("PX LAST");
        assertEquals(123, value.asInt());
    }

    @Test(groups = "unit")
    public void testGetData_SecurityMap() {
        DateTime date = new DateTime();
        data.add(date, "IBM", "PX LAST", 123);
        data.add(date, "IBM", "PX VOLUME", 456789);
        Map<String, TypedObject> values = data.forSecurity("IBM").forDate(date).get();
        assertEquals(2, values.size());
        assertEquals(123, values.get("PX LAST").asInt());
    }

    @Test(groups = "unit")
    public void testGetData_DateMap() {
        DateTime date1 = new DateTime();
        DateTime date2 = date1.minusDays(5);
        data.add(date1, "IBM", "PX LAST", 123);
        data.add(date2, "IBM", "PX LAST", 124);
        data.add(date1, "MSFT", "PX LAST", 456);
        data.add(date2, "MSFT", "PX LAST", 457);
        Map<DateTime, TypedObject> values = data.forSecurity("IBM").forField("PX LAST").get();
        assertEquals(2, values.size());
        assertEquals(124, values.get(date2).asInt());
    }

    @Test(groups = "unit")
    public void testGetData_Empty() {
        DateTime date = new DateTime();
        data.add(date, "IBM", "PX LAST", 123);

        assertFalse(data.forSecurity("IBM").get().isEmpty());

        assertTrue(data.forSecurity("ABC").get().isEmpty());
        assertTrue(data.forSecurity("ABC").forDate(date).get().isEmpty());
        assertTrue(data.forSecurity("ABC").forField("PX LAST").get().isEmpty());
        assertNull(data.forSecurity("ABC").forDate(date).forField("PX LAST"));
        assertNull(data.forSecurity("ABC").forField("PX LAST").forDate(date));

        assertTrue(data.forSecurity("IBM").forDate(date.minusDays(5)).get().isEmpty());
        assertNull(data.forSecurity("IBM").forDate(date.minusDays(5)).forField("PX LAST"));
        assertNull(data.forSecurity("IBM").forDate(date.minusDays(5)).forField("ABC"));

        assertTrue(data.forSecurity("IBM").forField("DEF").get().isEmpty());
        assertNull(data.forSecurity("IBM").forField("DEF").forDate(date));
        assertNull(data.forSecurity("IBM").forField("DEF").forDate(date.minusDays(5)));
    }

    @Test(groups = "unit")
    public void testToString() {
        //not really testing the output - just making sure no exception is thrown here
        assertFalse(data.toString().isEmpty());
        DateTime date = new DateTime();
        data.add(date, "IBM", "PX LAST", 123);
        data.add(date.plusDays(1), "IBM", "PX LAST", 124);
        data.add(date, "MSFT", "PX LAST", 456);
        data.add(date.plusDays(1), "MSFT", "PX LAST", 457);

        data.add(date, "IBM", "PX VOLUME", 123000);
        data.add(date.plusDays(1), "IBM", "PX VOLUME", 124000);
        data.add(date, "MSFT", "PX VOLUME", 456000);
        data.add(date.plusDays(1), "MSFT", "PX VOLUME", 457000);

        assertFalse(data.toString().isEmpty());

        data.addFieldError("UNKNOWN_FIELD");
        data.addSecurityError("UNKNOWN_TICKER");
        assertFalse(data.toString().isEmpty());
    }
}
