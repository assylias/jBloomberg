/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package assylias.jbloomberg;

import assylias.jbloomberg.ReferenceRequestBuilder;
import assylias.jbloomberg.DefaultBloombergSession;
import assylias.jbloomberg.DefaultBloombergSession.BloombergRequest;
import assylias.jbloomberg.DefaultBloombergSession.BloombergService;
import com.bloomberglp.blpapi.Request;
import com.bloomberglp.blpapi.Session;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.joda.time.DateTime;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Yann Le Tallec
 */
public class ReferenceRequestBuilderNGTest {

    @Test(groups="unit",expectedExceptions = NullPointerException.class)
    public void testConstructor_NullTickers() {
        new ReferenceRequestBuilder((Collection) null, Arrays.asList("a"));
    }

    @Test(groups="unit",expectedExceptions = NullPointerException.class)
    public void testConstructor_TickersContainsNull() {
        new ReferenceRequestBuilder(Arrays.<String>asList(null), Arrays.asList("a"));
    }

    @Test(groups="unit",expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*empty.*")
    public void testConstructor_EmptyTickers() {
        new ReferenceRequestBuilder(Collections.EMPTY_LIST, Arrays.asList("a"));
    }

    @Test(groups="unit",expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*empty\\sstrings.*")
    public void testConstructor_TickersContainsEmptyString() {
        new ReferenceRequestBuilder(Arrays.asList(""), Arrays.asList("a"));
    }

    @Test(groups="unit",expectedExceptions = NullPointerException.class)
    public void testConstructor_NullFields() {
        new ReferenceRequestBuilder(Arrays.asList("a"), (Collection) null);
    }

    @Test(groups="unit",expectedExceptions = NullPointerException.class)
    public void testConstructor_FieldsContainsNull() {
        new ReferenceRequestBuilder(Arrays.asList("a"), Arrays.<String>asList(null));
    }

    @Test(groups="unit",expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*empty.*")
    public void testConstructor_EmptyFields() {
        new ReferenceRequestBuilder(Arrays.asList("a"), Collections.EMPTY_LIST);
    }

    @Test(groups="unit",expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*empty\\sstrings.*")
    public void testConstructor_FieldsContainsEmptyString() {
        new ReferenceRequestBuilder(Arrays.asList("a"), Arrays.asList(""));
    }

    @Test(groups="unit",expectedExceptions = NullPointerException.class)
    public void testOverride_NullField() {
        ReferenceRequestBuilder builder = new ReferenceRequestBuilder("IBM US Equity", "PX_LAST");
        builder.addOverride(null, "asd");
    }

    @Test(groups="unit",expectedExceptions = NullPointerException.class)
    public void testOverride_NullValue() {
        ReferenceRequestBuilder builder = new ReferenceRequestBuilder("IBM US Equity", "PX_LAST");
        builder.addOverride("abc", null);
    }

    @Test(groups="unit",expectedExceptions = IllegalArgumentException.class)
    public void testOverride_EmptyField() {
        ReferenceRequestBuilder builder = new ReferenceRequestBuilder("IBM US Equity", "PX_LAST");
        builder.addOverride("", "asd");
    }

    @Test(groups="unit",expectedExceptions = IllegalArgumentException.class)
    public void testOverride_EmptyValue() {
        ReferenceRequestBuilder builder = new ReferenceRequestBuilder("IBM US Equity", "PX_LAST");
        builder.addOverride("abc", "");
    }

    @Test(groups="unit")
    public void testConstructor_AllOk() {
        new ReferenceRequestBuilder("IBM US Equity", "PX_LAST")
                .addOverride("abc", "def");
    }

    @Test(groups="unit")
    public void testServiceType() {
        assertEquals(new ReferenceRequestBuilder("ABC", "DEF").getServiceType(),
                DefaultBloombergSession.BloombergService.REFERENCE_DATA);
    }

    @Test(groups="unit")
    public void testRequestType() {
        assertEquals(new ReferenceRequestBuilder("ABC", "DEF").getRequestType(),
                DefaultBloombergSession.BloombergRequest.REFERENCE_DATA);
    }
}
