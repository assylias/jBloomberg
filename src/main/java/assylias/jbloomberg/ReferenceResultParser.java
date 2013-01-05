/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package assylias.jbloomberg;

import static assylias.jbloomberg.AbstractResultParser.SecurityDataElements.*;
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

    private void parseSecurityData(Element securityData) {
        String security = securityData.getElementAsString(SECURITY);
        if (securityData.hasElement(SECURITY_ERROR.asName(), true)) {
            Element errorInfo = securityData.getElement(SECURITY_ERROR.asName());
            logger.info("Security error on {}: {}", security, errorInfo);
            addSecurityError(security);
        } else if (securityData.hasElement(FIELD_EXCEPTIONS.asName(), true)) {
            Element fieldExceptionsArray = securityData.getElement(FIELD_EXCEPTIONS.asName());
            parseFieldExceptionsArray(fieldExceptionsArray);
        }
        if (securityData.hasElement(FIELD_DATA.asName(), true)) {
            Element fieldDataArray = securityData.getElement(FIELD_DATA.asName());
            parseFieldDataArray(security, fieldDataArray);
        }
    }

    /**
     * Adds the field exceptions to the RequestResult object. Assumes that one field can't generate more than one
     * exception.
     * In other words, we assume that if there are several exceptions, each corresponds to a different field.
     */
    private void parseFieldExceptionsArray(Element fieldExceptionsArray) {
        for (int i = 0; i < fieldExceptionsArray.numValues(); i++) {
            Element fieldException = fieldExceptionsArray.getValueAsElement(i);
            String field = fieldException.getElementAsString("fieldId");
            Element errorInfo = fieldException.getElement(ERROR_INFO);
            logger.info("Field exception on {}: {}", field, errorInfo);
            addFieldError(field);
        }
    }

    /**
     * There should be no more error at this point and we can happily parse the interesting portion of the response
     *
     */
    private void parseFieldDataArray(String security, Element fieldDataArray) {
        int numberOfFields = fieldDataArray.numElements();
        for (int i = 0; i < numberOfFields; i++) {
            Element field = fieldDataArray.getElement(i);
            addField(now, security, field);
        }
    }
}
