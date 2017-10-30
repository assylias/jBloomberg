/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import java.util.Map;
import java.util.TreeMap;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups="unit")
public class ReferenceDataTest {

    private ReferenceData data;

    @BeforeMethod
    public void beforeMethod() {
        data = new ReferenceData();
        data.add("IBM", "PRICE", 123.5);
        data.add("MSFT", "PRICE", 150);
        data.add("IBM", "NAME", "IBM Inc");
        data.add("MSFT", "NAME", "Microsoft");
    }

    @Test
    public void testIsEmpty() {
        assertTrue(new ReferenceData().isEmpty());
    }

    @Test
    public void testForField() {
        Map<String, Object> map = new TreeMap<> ();
        map.put("IBM", TypedObject.of(123.50));
        map.put("MSFT", TypedObject.of(150));
        assertEquals(data.forField("PRICE").get(), map);
        assertEquals(data.forField("PRICE").forSecurity("IBM").asDouble(), 123.5);
        assertEquals(data.forField("PRICE").forSecurity("MSFT").asInt(), 150);
        assertEquals(data.forField("NAME").forSecurity("IBM").asString(), "IBM Inc");
        assertEquals(data.forField("NAME").forSecurity("MSFT").asString(), "Microsoft");
    }

    @Test
    public void testForSecurity() {
        Map<String, Object> map = new TreeMap<> ();
        map.put("PRICE", TypedObject.of(123.50));
        map.put("NAME", TypedObject.of("IBM Inc"));
        assertEquals(data.forSecurity("IBM").get(), map);

        assertEquals(data.forField("PRICE").forSecurity("IBM"), data.forSecurity("IBM").forField("PRICE"));
        assertEquals(data.forField("PRICE").forSecurity("MSFT"), data.forSecurity("MSFT").forField("PRICE"));
        assertEquals(data.forField("NAME").forSecurity("IBM"), data.forSecurity("IBM").forField("NAME"));
        assertEquals(data.forField("NAME").forSecurity("MSFT"), data.forSecurity("MSFT").forField("NAME"));
    }
}
