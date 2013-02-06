/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package assylias.jbloomberg;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

@Test(groups="unit")
public class ReferenceRequestBuilderTest {

    @Test(expectedExceptions = NullPointerException.class)
    public void testConstructor_NullTickers() {
        new ReferenceRequestBuilder((Collection) null, Arrays.asList("a"));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testConstructor_TickersContainsNull() {
        new ReferenceRequestBuilder(Arrays.<String>asList(null), Arrays.asList("a"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*empty.*")
    public void testConstructor_EmptyTickers() {
        new ReferenceRequestBuilder(Collections.EMPTY_LIST, Arrays.asList("a"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*empty\\sstrings.*")
    public void testConstructor_TickersContainsEmptyString() {
        new ReferenceRequestBuilder(Arrays.asList(""), Arrays.asList("a"));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testConstructor_NullFields() {
        new ReferenceRequestBuilder(Arrays.asList("a"), (Collection) null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testConstructor_FieldsContainsNull() {
        new ReferenceRequestBuilder(Arrays.asList("a"), Arrays.<String>asList(null));
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*empty.*")
    public void testConstructor_EmptyFields() {
        new ReferenceRequestBuilder(Arrays.asList("a"), Collections.EMPTY_LIST);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*empty\\sstrings.*")
    public void testConstructor_FieldsContainsEmptyString() {
        new ReferenceRequestBuilder(Arrays.asList("a"), Arrays.asList(""));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testOverride_NullField() {
        ReferenceRequestBuilder builder = new ReferenceRequestBuilder("IBM US Equity", "PX_LAST");
        builder.addOverride(null, "asd");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testOverride_NullValue() {
        ReferenceRequestBuilder builder = new ReferenceRequestBuilder("IBM US Equity", "PX_LAST");
        builder.addOverride("abc", null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testOverride_EmptyField() {
        ReferenceRequestBuilder builder = new ReferenceRequestBuilder("IBM US Equity", "PX_LAST");
        builder.addOverride("", "asd");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testOverride_EmptyValue() {
        ReferenceRequestBuilder builder = new ReferenceRequestBuilder("IBM US Equity", "PX_LAST");
        builder.addOverride("abc", "");
    }

    @Test
    public void testConstructor_AllOk() {
        new ReferenceRequestBuilder("IBM US Equity", "PX_LAST")
                .addOverride("abc", "def");
    }

    @Test
    public void testServiceType() {
        assertEquals(new ReferenceRequestBuilder("ABC", "DEF").getServiceType(),
                BloombergServiceType.REFERENCE_DATA);
    }

    @Test
    public void testRequestType() {
        assertEquals(new ReferenceRequestBuilder("ABC", "DEF").getRequestType(),
                BloombergRequestType.REFERENCE_DATA);
    }
}
