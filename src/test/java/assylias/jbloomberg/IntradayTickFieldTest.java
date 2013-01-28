/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package assylias.jbloomberg;

import static org.testng.Assert.*;
import org.testng.annotations.Test;

@Test(groups = "unit")
public class IntradayTickFieldTest {

    @Test
    public void testOf() {
        IntradayTickField f = IntradayTickField.RPS_CODE;
        assertEquals(IntradayTickField.of("rpsCode"), f);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testOfExc() {
        IntradayTickField f = IntradayTickField.of("RPSCODE");
    }
}
