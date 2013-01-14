/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package assylias.jbloomberg;

/**
 * The URIs internally used by Bloomberg to identify the various services - users don't need to use these values
 * directly.
 */
public enum BloombergServiceType {

    REFERENCE_DATA("//blp/refdata"),
    MARKET_DATA("//blp/mktdata"),
    CUSTOM_VWAP("//blp/mktvwap"),
    MARKET_BAR("//blp/mktbar"),
    API_FIELD_INFORMATION("//blp/apiflds"),
    PAGE_DATA("//blp/pagedata"),
    TECHNICAL_ANALYSIS("//blp/tasvc"),
    API_AUTHORIZATION("//blp/apiauth");
    private final String serviceUri;

    BloombergServiceType(String serviceUri) {
        this.serviceUri = serviceUri;
    }

    /**
     * @return the uri of the service, for example "//blp/refdata"
     */
    public String getUri() {
        return serviceUri;
    }
}
