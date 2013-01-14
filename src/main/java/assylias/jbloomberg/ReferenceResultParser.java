/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package assylias.jbloomberg;

import com.bloomberglp.blpapi.Element;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * A ResultParser to parse the responses received from the Bloomberg Session when sending a Reference Data request.
 *
 * This implementation is thread safe as the Bloomberg API might send results through more than one thread.
 */
final class ReferenceResultParser extends AbstractResultParser {

    private static final Logger logger = LoggerFactory.getLogger(ReferenceResultParser.class);
    private final DateTime now = new DateTime();


    @Override
    protected void parseResponseNoResponseError(Element response) {
        if (response.hasElement(SECURITY_DATA, true)) {
            Element securityDataArray = response.getElement(SECURITY_DATA);
            parseSecurityDataArray(securityDataArray);
        }
    }

    private void parseSecurityDataArray(Element securityDataArray) {
        int numSecurities = securityDataArray.numValues();
        for (int i = 0; i < numSecurities; i++) {
            Element securityData = securityDataArray.getValueAsElement(i);
            parseSecurityData(securityData);
        }
    }

    /**
     * There should be no more error at this point and we can happily parse the interesting portion of the response
     *
     */
    @Override
    protected void parseFieldDataArray(String security, Element fieldDataArray) {
        int numberOfFields = fieldDataArray.numElements();
        for (int i = 0; i < numberOfFields; i++) {
            Element field = fieldDataArray.getElement(i);
            addField(now, security, field);
        }
    }
}
