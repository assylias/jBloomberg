/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package assylias.jbloomberg;

import com.google.common.collect.Multimap;
import java.util.Map;
import java.util.TreeMap;
import org.joda.time.DateTime;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class IntradayTickDataTest {

    private IntradayTickData data;
    private final int[] values = {1, 2, 3, 4, 3, 5, 6, 7};

    @BeforeMethod
    public void beforeMethod() {
        data = new IntradayTickData("ABC");
        DateTime dt  = DateTime.now();
        int i = 0;
        data.add(dt.minusMillis(200), "value", values[i++]);
        data.add(dt.minusMillis(150), "value", values[i++]);
        data.add(dt.minusMillis(100), "value", values[i++]);
        data.add(dt.minusMillis(100), "value", values[i++]);
        data.add(dt.minusMillis(100), "value", values[i++]);
        data.add(dt, "value", values[i++]);
        data.add(dt, "value", values[i++]);
        data.add(dt.plusMillis(200), "value", values[i++]);
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
        Multimap<DateTime, Object> result = data.forField(IntradayTickField.VALUE);
        assertEquals(result.size(), 8);
    }

    @Test
    public void testForField_Order() {
        Multimap<DateTime, Object> result = data.forField(IntradayTickField.VALUE);
        int i = 0;
        for (Object o : result.values()) {
            assertEquals(o, values[i++]);
        }
    }
}
