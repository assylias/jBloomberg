/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package assylias.jbloomberg;

import static assylias.jbloomberg.AbstractResultParser.SecurityDataElements.*;
import com.bloomberglp.blpapi.Element;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * A ResultParser to parse the responses received from the Bloomberg Session when sending a Historical Data request.
 *
 * This implementation is thread safe as the Bloomberg API might send results through more than one thread.
 */
final class HistoricalResultParser extends AbstractResultParser {

    private static final Logger logger = LoggerFactory.getLogger(HistoricalResultParser.class);
    private static final DateTimeFormatter BLOOMBERG_DATE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd");

    @Override
    protected void parseResponseNoResponseError(Element response) {
        if (response.hasElement(SECURITY_DATA, true)) {
            Element securityData = response.getElement(SECURITY_DATA);
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
     * Adds the field exceptions to the HistoricalData object. Assumes that one field can't generate more than one
     * exception.
     * In other words, if there are several exceptions, each corresponds to a different field.
     */
    private void parseFieldExceptionsArray(Element fieldExceptionsArray) {
        for (int i = 0; i < fieldExceptionsArray.numValues(); i++) {
            Element fieldException = fieldExceptionsArray.getValueAsElement(i);
            String field = fieldException.getElementAsString("fieldId");
            Element errorInfo = fieldException.getElement(ERROR_INFO);
            logger.info("Field exception on {}: {}", field, errorInfo);
            String response = parseErrorInfo(errorInfo);
            addFieldError(field);
        }
    }

    /**
     * There should be no more error at this point and we can happily parse the interesting portion of the response
     *
     */
    private void parseFieldDataArray(String security, Element fieldDataArray) {
        int countData = fieldDataArray.numValues();
        for (int i = 0; i < countData; i++) {
            Element fieldData = fieldDataArray.getValueAsElement(i);
            Element field = fieldData.getElement(0);
            if (!DATE.equals(field.name())) {
                throw new AssertionError("Date field is supposed to be first but got: " + field.name());
            }
            DateTime date = BLOOMBERG_DATE_FORMATTER.parseDateTime(field.getValueAsString());

            for (int j = 1; j < fieldData.numElements(); j++) {
                field = fieldData.getElement(j);
                addField(date, security, field);
            }
        }
    }
}
