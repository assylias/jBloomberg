/*
 * Copyright 2013 Yann Le Tallec.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
public abstract class AbstractRequestBuilder implements RequestBuilder {

    protected final static DateTimeFormatter BB_REQUEST_DATE_FORMATTER = DateTimeFormat.forPattern("yyyyMMdd");
    protected final static DateTimeFormatter BB_REQUEST_DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Override
    public Request buildRequest(Session session) {
        Service service = session.getService(getServiceType().getUri());
        Request request = service.createRequest(getRequestType().toString());
        buildRequest(request);
        return request;
    }

    protected void addCollectionToElement(Request request, Iterable<String> collection, String elementName) {
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
