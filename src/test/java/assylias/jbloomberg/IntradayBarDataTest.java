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
package assylias.jbloomberg;

import org.joda.time.DateTime;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Yann Le Tallec
 */
public class IntradayBarDataTest {

    private IntradayBarData data;
    private final int[] values = {1, 2, 3, 4, 5, 6, 7, 8};
    private final DateTime dt = DateTime.now();

    @BeforeMethod
    public void beforeMethod() {
        data = new IntradayBarData("ABC");

        int i = 0;
        data.add(dt.minusMillis(200), "open", values[i++]);
        data.add(dt.minusMillis(200), "high", values[i++]);
        data.add(dt.minusMillis(200), "low", values[i++]);
        data.add(dt.minusMillis(200), "close", values[i++]);
        data.add(dt.minusMillis(100), "open", values[i++]);
        data.add(dt.minusMillis(100), "high", values[i++]);
        data.add(dt.minusMillis(100), "low", values[i++]);
        data.add(dt.minusMillis(100), "close", values[i++]);
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
        data.add(DateTime.now(), "asdkjh", 213);
        assertEquals(data.get().size(), size); //nothing added
    }

    @Test
    public void testForField() {
        IntradayBarData.ResultForField result = data.forField(IntradayBarField.CLOSE);
        assertEquals(result.get().size(), 2);
        assertEquals(result.forDate(dt.minusMillis(200)), 4);
        assertEquals(result.forDate(dt.minusMillis(100)), 8);
    }

    @Test
    public void testForDate() {
        IntradayBarData.ResultForDate result = data.forDate(dt.minusMillis(200));
        assertEquals(result.get().size(), 4);
        assertEquals(result.forField(IntradayBarField.OPEN), 1);
        assertEquals(result.forField(IntradayBarField.HIGH), 2);
        assertEquals(result.forField(IntradayBarField.LOW), 3);
        assertEquals(result.forField(IntradayBarField.CLOSE), 4);

        assertEquals(data.forField(IntradayBarField.CLOSE).forDate(dt.minusMillis(100)),
                data.forDate(dt.minusMillis(100)).forField(IntradayBarField.CLOSE));
    }
}
