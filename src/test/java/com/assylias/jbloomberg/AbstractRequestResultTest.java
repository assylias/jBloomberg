/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Test(groups = "unit")
public class AbstractRequestResultTest {


    private AbstractRequestResult data;

    @BeforeMethod
    public void beforeMethod() {
        data = new AbstractRequestResultImpl();
    }

    @Test
    public void testHasErrors_None() {
        assertFalse(data.hasErrors());
    }

    @Test
    public void testHasError_Security() {
        data.addSecurityError("Sec");
        assertTrue(data.hasErrors());
        assertTrue(data.getSecurityErrors().contains("Sec"));
    }

    @Test
    public void testHasError_Field() {
        data.addFieldError("Field");
        assertTrue(data.hasErrors());
        assertTrue(data.getFieldErrors().contains("Field"));
    }

    public class AbstractRequestResultImpl extends AbstractRequestResult {
        @Override
        public boolean isEmpty() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
