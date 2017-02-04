/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.Datetime;
import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Schema;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import static org.testng.Assert.*;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class BloombergUtilsTest {

    @Test(groups = "unit")
    public void test_ProcessRunning() {
        setBbcommStartedFlag(false);
        new MockUp<ShellUtils>() {
            @Mock(invocations = 1)
            public boolean isProcessRunning(String processName) {
                return true;
            }
        };
        assertTrue(BloombergUtils.startBloombergProcessIfNecessary());
    }

    @Test(groups = "unit")
    public void test_NoRetryOnceRunning() {
        setBbcommStartedFlag(true);
        new MockUp<ShellUtils>() {
            @Mock(invocations = 0)
            public boolean isProcessRunning(String processName) {
                return true;
            }
        };
        assertTrue(BloombergUtils.startBloombergProcessIfNecessary());
    }

    @Test(groups = "unit")
    public void test_ProcessNotRunning_StartBbCommSucceeds(@Mocked final ProcessBuilder pb, @Mocked final Process p) throws IOException {
        setBbcommStartedFlag(false);
        new MockUp<ShellUtils>() {
            @Mock
            public boolean isProcessRunning(String processName) {
                return false;
            }
        };
        new NonStrictExpectations() {
            {
                pb.start();
                result = p;
                p.getInputStream();
                result = new ByteArrayInputStream("started".getBytes());
            }
        };
        assertTrue(BloombergUtils.startBloombergProcessIfNecessary());
    }

    @Test(groups = "unit")
    public void test_ProcessNotRunning_StartBbCommFails(@Mocked final ProcessBuilder pb, @Mocked final Process p) throws IOException {
        setBbcommStartedFlag(false);
        new MockUp<ShellUtils>() {
            @Mock
            public boolean isProcessRunning(String processName) {
                return false;
            }
        };
        new NonStrictExpectations() {
            {
                pb.start();
                result = p;
                p.getInputStream();
                result = new ByteArrayInputStream("whatever".getBytes());
            }
        };
        assertFalse(BloombergUtils.startBloombergProcessIfNecessary());
    }


    @Test(groups = "requires-bloomberg")
    public void testBbStart() throws Exception {
        setBbcommStartedFlag(false);
        assertTrue(BloombergUtils.startBloombergProcessIfNecessary());
    }

    private static void setBbcommStartedFlag(boolean flag) {
        try {
            Field f = BloombergUtils.class.getDeclaredField("isBbcommStarted");
            f.setAccessible(true);
            f.set(null, flag);
        } catch (Exception e) {
            fail();
        }
    }

    /**
     * From January 16th 2017, the real time field "PROXY_TIME_OF_LAST_UPDATE_RT" has started sending back data with a time datatype but containing dates.
     */
    @Test(groups = "unit")
    public void test_invalid_time_issue_18() {
      Element mockElement = new MockUp<Element>() {
        @Mock public Schema.Datatype datatype() { return Schema.Datatype.TIME; }
        @Mock public Datetime getValueAsDatetime() { return new Datetime(2017, 1, 1); }
      }.getMockInstance();

      assertEquals(BloombergUtils.getSpecificObjectOf(mockElement), LocalDate.of(2017, 1, 1));
    }

    @Test(groups = "unit")
    public void getSpecificObjectOf_primitives() {
      Element mockElement;

      mockElement = new MockUp<Element>() {
        @Mock public Schema.Datatype datatype() { return Schema.Datatype.BOOL; }
        @Mock public boolean getValueAsBool() { return true; }
      }.getMockInstance();
      assertEquals(BloombergUtils.getSpecificObjectOf(mockElement).getClass(), Boolean.class);
      assertEquals(BloombergUtils.getSpecificObjectOf(mockElement), true);

      mockElement = new MockUp<Element>() {
        @Mock public Schema.Datatype datatype() { return Schema.Datatype.CHAR; }
        @Mock public char getValueAsChar() { return 'a'; }
      }.getMockInstance();
      assertEquals(BloombergUtils.getSpecificObjectOf(mockElement).getClass(), Character.class);
      assertEquals(BloombergUtils.getSpecificObjectOf(mockElement), 'a');

      mockElement = new MockUp<Element>() {
        @Mock public Schema.Datatype datatype() { return Schema.Datatype.FLOAT32; }
        @Mock public float getValueAsFloat32() { return 1f; }
      }.getMockInstance();
      assertEquals(BloombergUtils.getSpecificObjectOf(mockElement).getClass(), Float.class);
      assertEquals(BloombergUtils.getSpecificObjectOf(mockElement), 1f);

      mockElement = new MockUp<Element>() {
        @Mock public Schema.Datatype datatype() { return Schema.Datatype.FLOAT64; }
        @Mock public double getValueAsFloat64() { return 1d; }
      }.getMockInstance();
      assertEquals(BloombergUtils.getSpecificObjectOf(mockElement).getClass(), Double.class);
      assertEquals(BloombergUtils.getSpecificObjectOf(mockElement), 1d);

      mockElement = new MockUp<Element>() {
        @Mock public Schema.Datatype datatype() { return Schema.Datatype.INT32; }
        @Mock public int getValueAsInt32() { return 1; }
      }.getMockInstance();
      assertEquals(BloombergUtils.getSpecificObjectOf(mockElement).getClass(), Integer.class);
      assertEquals(BloombergUtils.getSpecificObjectOf(mockElement), 1);

      mockElement = new MockUp<Element>() {
        @Mock public Schema.Datatype datatype() { return Schema.Datatype.INT64; }
        @Mock public long getValueAsInt64() { return 1L; }
      }.getMockInstance();
      assertEquals(BloombergUtils.getSpecificObjectOf(mockElement).getClass(), Long.class);
      assertEquals(BloombergUtils.getSpecificObjectOf(mockElement), 1L);

      mockElement = new MockUp<Element>() {
        @Mock public Schema.Datatype datatype() { return Schema.Datatype.STRING; }
        @Mock public String getValueAsString() { return "a"; }
      }.getMockInstance();
      assertEquals(BloombergUtils.getSpecificObjectOf(mockElement).getClass(), String.class);
      assertEquals(BloombergUtils.getSpecificObjectOf(mockElement), "a");
    }

    @DataProvider(name = "datatypes") public Object[][] datatypes() {
      return new Object[][] {
              { Schema.Datatype.DATE },
              { Schema.Datatype.TIME },
              { Schema.Datatype.DATETIME },
      };
    }

    /**
     * We simply ignore the data type as it may be wrong. Instead check on the parts that are present in the Datetime object.
     */
    @Test(groups = "unit", dataProvider = "datatypes")
    public void getSpecificObjectOf_dates(Schema.Datatype datatype) {
      Element mockElement;

      mockElement = new MockUp<Element>() {
        @Mock public Schema.Datatype datatype() { return datatype; }
        @Mock public Datetime getValueAsDatetime() { return new Datetime(2017, 1, 1); }
      }.getMockInstance();
      assertEquals(BloombergUtils.getSpecificObjectOf(mockElement).getClass(), LocalDate.class);
      assertEquals(BloombergUtils.getSpecificObjectOf(mockElement), LocalDate.of(2017, 1, 1));

      mockElement = new MockUp<Element>() {
        @Mock public Schema.Datatype datatype() { return datatype; }
        @Mock public Datetime getValueAsDatetime() { return new Datetime(2017, 1, 1, 13, 0, 0, 0); }
      }.getMockInstance();
      assertEquals(BloombergUtils.getSpecificObjectOf(mockElement).getClass(), OffsetDateTime.class);
      assertEquals(BloombergUtils.getSpecificObjectOf(mockElement), OffsetDateTime.of(2017, 1, 1, 13, 0, 0, 0, ZoneOffset.UTC));

      mockElement = new MockUp<Element>() {
        @Mock public Schema.Datatype datatype() { return datatype; }
        @Mock public Datetime getValueAsDatetime() { Datetime dt = new Datetime(2017, 1, 1, 13, 0, 0, 0); dt.setTimezoneOffsetMinutes(60); return dt; }
      }.getMockInstance();
      assertEquals(BloombergUtils.getSpecificObjectOf(mockElement).getClass(), OffsetDateTime.class);
      assertEquals(BloombergUtils.getSpecificObjectOf(mockElement), OffsetDateTime.of(2017, 1, 1, 13, 0, 0, 0, ZoneOffset.ofHours(1)));

      mockElement = new MockUp<Element>() {
        @Mock public Schema.Datatype datatype() { return datatype; }
        @Mock public Datetime getValueAsDatetime() { Datetime dt = new Datetime(13, 0, 0, 0); return dt; }
      }.getMockInstance();
      assertEquals(BloombergUtils.getSpecificObjectOf(mockElement).getClass(), OffsetTime.class);
      assertEquals(BloombergUtils.getSpecificObjectOf(mockElement), OffsetTime.of(13, 0, 0, 0, ZoneOffset.UTC));

      mockElement = new MockUp<Element>() {
        @Mock public Schema.Datatype datatype() { return datatype; }
        @Mock public Datetime getValueAsDatetime() { Datetime dt = new Datetime(13, 0, 0, 0); dt.setTimezoneOffsetMinutes(60); return dt; }
      }.getMockInstance();
      assertEquals(BloombergUtils.getSpecificObjectOf(mockElement).getClass(), OffsetTime.class);
      assertEquals(BloombergUtils.getSpecificObjectOf(mockElement), OffsetTime.of(13, 0, 0, 0, ZoneOffset.ofHours(1)));
    }

    //TODO: add tests for CHOICE, BYTEARRAY and SEQUENCE
}
