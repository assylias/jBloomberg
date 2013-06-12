/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import static org.testng.Assert.*;
import org.testng.annotations.Test;

@Test(groups = "unit")
public class IntradayBarFieldTest {

    @Test
    public void testOf() {
        IntradayBarField f = IntradayBarField.CLOSE;
        assertEquals(IntradayBarField.of("close"), f);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testOfExc() {
        IntradayBarField f = IntradayBarField.of("CLOSE");
    }
}
