/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package assylias.jbloomberg;

import static org.testng.Assert.*;
import org.testng.annotations.Test;

public class RealtimeFieldTest {

    @Test(groups = "unit")
    public void testContains() {
        String aFieldThatExists = RealtimeField.ALL_PRICE.toString();
        String aFieldThatDoesNotExist = "A FIELD THAT DOES NOT EXIST OU BIEN?";

        assertTrue(RealtimeField.contains(aFieldThatExists));
        assertFalse(RealtimeField.contains(aFieldThatDoesNotExist));
    }

    @Test(groups = "unit")
    public void testContainsIgnoreCase() {
        String aFieldThatExists = "bid";
        assertFalse(RealtimeField.contains(aFieldThatExists));
        assertTrue(RealtimeField.containsIgnoreCase(aFieldThatExists));
    }
}
