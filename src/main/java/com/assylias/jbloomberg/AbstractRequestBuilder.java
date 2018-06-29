/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Request;
import com.bloomberglp.blpapi.Service;
import com.bloomberglp.blpapi.Session;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 *
 */
abstract class AbstractRequestBuilder<T extends RequestResult> implements RequestBuilder<T> {

    final static DateTimeFormatter BB_REQUEST_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE; //'20111203'
    final static DateTimeFormatter BB_REQUEST_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME; //'2011-12-03T10:15:30'

    @Override
    public Request buildRequest(Session session) {
        Service service = session.getService(getServiceType().getUri());
        Request request = service.createRequest(getRequestType().toString());
        buildRequest(request);
        return request;
    }

    static void addCollectionToElement(Request request, Iterable<String> collection, String elementName) {
        Element element = request.getElement(elementName);
        for (String item : collection) {
            element.appendValue(item);
        }
    }

    static void addValueToElement(Request request, String elementValue, String elementName) {
        Element element = request.getElement(elementName);
        element.setValue(elementValue);

    }

    static void addOverrides(Request request, Map<String, String> overrides) {
        Element overridesElt = request.getElement("overrides");
        for (Map.Entry<String, String> e : overrides.entrySet()) {
            Element override = overridesElt.appendElement();
            override.setElement("fieldId", e.getKey());
            override.setElement("value", e.getValue());
        }
    }

    /**
     *
     * @param request an empty Request that needs to be populated
     */
    protected abstract void buildRequest(Request request);
}
