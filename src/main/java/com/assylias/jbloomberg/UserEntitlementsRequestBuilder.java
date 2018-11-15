package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Request;

public class UserEntitlementsRequestBuilder extends AbstractRequestBuilder<UserEntitlements> {
    private final int uuid;

    public UserEntitlementsRequestBuilder(int uuid) {
        this.uuid = uuid;
    }

    @Override
    public BloombergServiceType getServiceType() {
        return BloombergServiceType.API_AUTHORIZATION;
    }

    @Override
    public BloombergRequestType getRequestType() {
        return BloombergRequestType.USER_ENTITLEMENTS;
    }

    @Override
    protected void buildRequest(final Request request) {
        Element userInfo = request.getElement("userInfo");
        userInfo.setElement("uuid", uuid);
    }

    @Override
    public ResultParser<UserEntitlements> getResultParser() {
        return new UserEntitlementsResultParser();
    }
}
