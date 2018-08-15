/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Test(groups = "unit")
public class RealtimeFieldTest {

    @Test
    public void testContains() {
        String aFieldThatExists = RealtimeField.ALL_PRICE.toString();
        String aFieldThatDoesNotExist = "A FIELD THAT DOES NOT EXIST OU BIEN?";

        assertTrue(RealtimeField.contains(aFieldThatExists));
        assertFalse(RealtimeField.contains(aFieldThatDoesNotExist));
    }

    @Test
    public void testContainsIgnoreCase() {
        String aFieldThatExists = "bid";
        assertFalse(RealtimeField.contains(aFieldThatExists));
        assertTrue(RealtimeField.containsIgnoreCase(aFieldThatExists));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testContains_null() {
        assertFalse(RealtimeField.contains(null));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testContainsIgnoreCase_null() {
        assertFalse(RealtimeField.containsIgnoreCase(null));
    }
}
