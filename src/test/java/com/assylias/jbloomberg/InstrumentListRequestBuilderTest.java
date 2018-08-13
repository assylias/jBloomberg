package com.assylias.jbloomberg;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

@Test(groups="unit")
public class InstrumentListRequestBuilderTest {

    @Test(expectedExceptions = NullPointerException.class)
    public void testConstructor_NullQuery() {
        new InstrumentListRequestBuilder(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testConstructor_ZeroMaxResults() {
        new InstrumentListRequestBuilder("", 0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testConstructor_NegativeMaxResults() {
        new InstrumentListRequestBuilder("", -10);
    }

    @Test
    public void testConstructor_AllowsEmptyString() {
        new InstrumentListRequestBuilder("");
    }

    @Test
    public void testServiceType() {
        assertEquals(new InstrumentListRequestBuilder("").getServiceType(), BloombergServiceType.INSTRUMENTS);
    }

    @Test
    public void testRequestType() {
        assertEquals(new InstrumentListRequestBuilder("").getRequestType(), BloombergRequestType.INSTRUMENT_LIST);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testYellowKeyFilter_NullValue() {
        InstrumentListRequestBuilder builder = new InstrumentListRequestBuilder("");
        builder.withYellowKeyFilter(null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testLanguageOverride_NullValue() {
        InstrumentListRequestBuilder builder = new InstrumentListRequestBuilder("");
        builder.withLanguageOverride(null);
    }
}
