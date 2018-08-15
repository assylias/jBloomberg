/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.google.common.collect.Multimap;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = "unit")
public class IntradayTickDataTest {

    private static final OffsetDateTime NOW = OffsetDateTime.now();
    private IntradayTickData data;
    private final int[] values = {1, 2, 3, 4, 3, 5, 6, 7};

    @BeforeMethod
    public void beforeMethod() {
        data = new IntradayTickData("ABC");
        int i = 0;
        data.add(NOW.minus(200, ChronoUnit.MILLIS), "value", values[i++]);
        data.add(NOW.minus(150, ChronoUnit.MILLIS), "value", values[i++]);
        data.add(NOW.minus(100, ChronoUnit.MILLIS), "value", values[i++]);
        data.add(NOW.minus(100, ChronoUnit.MILLIS), "value", values[i++]);
        data.add(NOW.minus(100, ChronoUnit.MILLIS), "value", values[i++]);
        data.add(NOW, "value", values[i++]);
        data.add(NOW, "value", values[i++]);
        data.add(NOW.plus(200, ChronoUnit.MILLIS), "value", values[i++]);
    }

    @Test
    public void testIsEmpty() {
        assertTrue(new IntradayTickData("ABC").isEmpty());
    }

    @Test
    public void testGetSecurity() {
        assertEquals(data.getSecurity(), "ABC");
    }

    @Test
    public void testForField_Size() {
        Multimap<OffsetDateTime, TypedObject> result = data.forField(IntradayTickField.VALUE);
        assertEquals(result.size(), 8);
    }

    @Test
    public void testForField_Order() {
        Multimap<OffsetDateTime, TypedObject> result = data.forField(IntradayTickField.VALUE);
        int i = 0;
        for (TypedObject o : result.values()) {
            assertEquals(o.asInt(), values[i++]);
        }
    }
}
