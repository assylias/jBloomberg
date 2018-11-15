package com.assylias.jbloomberg;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertTrue;

@Test(groups = "requires-bloomberg")
public class UserEntitlementsResultParserTest {

    private DefaultBloombergSession session = null;

    @BeforeClass
    public void beforeClass() throws BloombergException {
        session = new DefaultBloombergSession();
        session.start();
    }

    @AfterClass
    public void afterClass() throws BloombergException {
        if (session != null) {
            session.stop();
        }
    }

    @Test
    public void testLookupEntitlements_InvalidUser() throws Exception {
        UserEntitlementsRequestBuilder uerb = new UserEntitlementsRequestBuilder(1);
        UserEntitlements entitlements = session.submit(uerb).get(2, TimeUnit.SECONDS);
        assertTrue(entitlements.getEids().isEmpty());
    }
}
