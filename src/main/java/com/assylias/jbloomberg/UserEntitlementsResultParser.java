package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Name;

public class UserEntitlementsResultParser extends AbstractResultParser<UserEntitlements> {
    private static final Name USERENTITLEMENTS_RESPONSE = Name.getName("UserEntitlementsResponse");

    @Override
    protected UserEntitlements getRequestResult() {
        return new UserEntitlements();
    }

    @Override
    protected void parseResponseNoError(final Element response, final UserEntitlements result) {
        if (response.name().equals(USERENTITLEMENTS_RESPONSE)) {
            Element returnedEids = response.getElement("eids");
            int numeids = returnedEids.numValues();
            for (int i = 0; i < numeids; ++i) {
                result.addPermission(returnedEids.getValueAsInt32(i));
            }
        }
    }
}
