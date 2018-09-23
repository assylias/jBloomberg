package com.assylias.jbloomberg;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Set;

import static org.testng.Assert.assertTrue;

@Test(groups = "unit")
public class UserEntitlementsTest {
    private UserEntitlements data;

    @BeforeMethod
    public void beforeMethod() {
        data = new UserEntitlements();
        data.addPermission(1);
        data.addPermission(3);
    }

    @Test
    public void testIsEmpty() {
        assertTrue(new UserEntitlements().isEmpty());
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testResultIsImmutable() {
        final Set<Integer> eids = data.getEids();
        eids.remove(0);
    }
}
