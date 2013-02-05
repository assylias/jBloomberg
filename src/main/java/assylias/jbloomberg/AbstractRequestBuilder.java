/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package assylias.jbloomberg;

import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Request;
import com.bloomberglp.blpapi.Service;
import com.bloomberglp.blpapi.Session;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 */
abstract class AbstractRequestBuilder<T extends RequestResult> implements RequestBuilder<T> {

    final static DateTimeFormatter BB_REQUEST_DATE_FORMATTER = DateTimeFormat.forPattern("yyyyMMdd");
    final static DateTimeFormatter BB_REQUEST_DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");

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

    /**
     *
     * @param request an empty Request that needs to be populated
     */
    protected abstract void buildRequest(Request request);
}
