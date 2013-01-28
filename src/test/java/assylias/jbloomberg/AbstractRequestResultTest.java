/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package assylias.jbloomberg;

import static org.testng.Assert.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AbstractRequestResultTest {


    private AbstractRequestResult data;

    @BeforeMethod(groups = "unit")
    public void beforeMethod() {
        data = new AbstractRequestResultImpl();
    }

    @Test(groups = "unit")
    public void testHasErrors_None() {
        assertFalse(data.hasErrors());
    }

    @Test(groups = "unit")
    public void testHasError_Security() {
        data.addSecurityError("Sec");
        assertTrue(data.hasErrors());
        assertTrue(data.getSecurityErrors().contains("Sec"));
    }

    @Test(groups = "unit")
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
