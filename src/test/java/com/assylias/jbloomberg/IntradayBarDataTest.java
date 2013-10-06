/*
 * Copyright 2013 Yann Le Tallec.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.assylias.jbloomberg;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Yann Le Tallec
 */
public class IntradayBarDataTest {

    private static final LocalDateTime NOW = LocalDateTime.now();
    private IntradayBarData data;
    private final int[] values = {1, 2, 3, 4, 5, 6, 7, 8};

    @BeforeMethod
    public void beforeMethod() {
        data = new IntradayBarData("ABC");

        int i = 0;
        data.add(NOW.minus(200, ChronoUnit.MILLIS), "open", values[i++]);
        data.add(NOW.minus(200, ChronoUnit.MILLIS), "high", values[i++]);
        data.add(NOW.minus(200, ChronoUnit.MILLIS), "low", values[i++]);
        data.add(NOW.minus(200, ChronoUnit.MILLIS), "close", values[i++]);
        data.add(NOW.minus(100, ChronoUnit.MILLIS), "open", values[i++]);
        data.add(NOW.minus(100, ChronoUnit.MILLIS), "high", values[i++]);
        data.add(NOW.minus(100, ChronoUnit.MILLIS), "low", values[i++]);
        data.add(NOW.minus(100, ChronoUnit.MILLIS), "close", values[i++]);
    }

    @Test
    public void testIsEmpty() {
        assertTrue(new IntradayBarData("ABC").isEmpty());
    }

    @Test
    public void testGetSecurity() {
        assertEquals(data.getSecurity(), "ABC");
    }

    @Test
    public void testAddWrongField_NoException() {
        int size = data.get().size();
        data.add(NOW, "asdkjh", 213);
        assertEquals(data.get().size(), size); //nothing added
    }

    @Test
    public void testForField() {
        IntradayBarData.ResultForField result = data.forField(IntradayBarField.CLOSE);
        assertEquals(result.get().size(), 2);
        assertEquals(result.forDate(NOW.minus(200, ChronoUnit.MILLIS)).asInt(), 4);
        assertEquals(result.forDate(NOW.minus(100, ChronoUnit.MILLIS)).asInt(), 8);
    }

    @Test
    public void testForDate() {
        IntradayBarData.ResultForDate result = data.forDate(NOW.minus(200, ChronoUnit.MILLIS));
        assertEquals(result.get().size(), 4);
        assertEquals(result.forField(IntradayBarField.OPEN).asInt(), 1);
        assertEquals(result.forField(IntradayBarField.HIGH).asInt(), 2);
        assertEquals(result.forField(IntradayBarField.LOW).asInt(), 3);
        assertEquals(result.forField(IntradayBarField.CLOSE).asInt(), 4);

        assertEquals(data.forField(IntradayBarField.CLOSE).forDate(NOW.minus(100, ChronoUnit.MILLIS)),
                data.forDate(NOW.minus(100, ChronoUnit.MILLIS)).forField(IntradayBarField.CLOSE));
    }
}
