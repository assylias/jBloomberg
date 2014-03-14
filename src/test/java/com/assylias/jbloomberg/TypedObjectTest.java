/*
 * Copyright (C) 2013 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Yann Le Tallec
 */
public class TypedObjectTest {

    @Test(expectedExceptions = NullPointerException.class)
    public void noNull() {
        TypedObject.of(null);
    }

    @Test
    public void asInt() {
        assertEquals(TypedObject.of(123).asInt(), 123);
    }

    @Test
    public void asIntNumber() {
        assertEquals(TypedObject.of(123.456d).asInt(), 123);
        assertEquals(TypedObject.of(BigDecimal.TEN).asInt(), 10);
    }

    @Test(expectedExceptions = ClassCastException.class)
    public void asIntError() {
        TypedObject.of("asd").asInt();
    }

    @Test
    public void asDouble() {
        assertEquals(TypedObject.of(123.456d).asDouble(), 123.456d);
    }

    @Test
    public void asDoubleNumber() {
        assertEquals(TypedObject.of(123).asDouble(), 123d);
        assertEquals(TypedObject.of(BigDecimal.TEN).asDouble(), 10d);
    }

    @Test(expectedExceptions = ClassCastException.class)
    public void asDoubleError() {
        TypedObject.of("asd").asInt();
    }

    @Test
    public void asString() {
        assertEquals(TypedObject.of("123").asString(), "123");
        assertEquals(TypedObject.of(123).asString(), "123");
        assertEquals(TypedObject.of(123d).asString(), "123.0");
        assertEquals(TypedObject.of("123").toString(), "123");
        assertEquals(TypedObject.of(123).toString(), "123");
        assertEquals(TypedObject.of(123d).toString(), "123.0");
    }
    
    @Test
    public void asList() {
        List<TypedObject> list = Arrays.asList(TypedObject.of("a"), TypedObject.of("b"));
        TypedObject o = TypedObject.of(list);
        assertEquals(o.asList().get(0).asString(), "a");
        assertTrue(o.isList());
    }

    @Test
    public void asListClass() {
        List<String> list = Arrays.asList("a", "b");
        TypedObject o = TypedObject.of(list);
        assertEquals(o.asList(String.class).get(0), "a");
        assertTrue(o.isList());
    }

    @Test
    public void hashcode() {
        Object o = new Object();
        assertEquals(TypedObject.of(o).hashCode(), o.hashCode());
    }

    @Test
    public void equals() {
        Object o = new Object();
        assertTrue(TypedObject.of(o).equals(TypedObject.of(o)));
        assertFalse(TypedObject.of(o).equals(null));
    }
}