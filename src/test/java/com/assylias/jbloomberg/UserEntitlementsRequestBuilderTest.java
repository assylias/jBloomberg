package com.assylias.jbloomberg;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

@Test(groups="unit")
public class UserEntitlementsRequestBuilderTest {
    @Test
    public void testServiceType() {
        assertEquals(new UserEntitlementsRequestBuilder(1).getServiceType(), BloombergServiceType.API_AUTHORIZATION);
    }

    @Test
    public void testRequestType() {
        assertEquals(new UserEntitlementsRequestBuilder(1).getRequestType(), BloombergRequestType.USER_ENTITLEMENTS);
    }

}
