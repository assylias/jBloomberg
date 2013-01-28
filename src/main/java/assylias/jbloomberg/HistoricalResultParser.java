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
 * A ResultParser to parse the responses received from the Bloomberg Session when sending a Historical Data request.
 *
 * This implementation is thread safe as the Bloomberg API might send results through more than one thread.
 */
final class HistoricalResultParser extends AbstractResultParser<HistoricalData> {

    private static final Logger logger = LoggerFactory.getLogger(HistoricalResultParser.class);

    @Override
    protected HistoricalData getRequestResult() {
        return new HistoricalData();
    }

    @Override
    protected void parseResponseNoResponseError(Element response) {
        if (response.hasElement(SECURITY_DATA, true)) {
            Element securityData = response.getElement(SECURITY_DATA);
            parseSecurityData(securityData);
        }
    }

    /**
     * There should be no more error at this point and we can happily parse the interesting portion of the response
     *
     */
    @Override
    protected void parseFieldDataArray(String security, Element fieldDataArray) {
        int countData = fieldDataArray.numValues();
        for (int i = 0; i < countData; i++) {
            Element fieldData = fieldDataArray.getValueAsElement(i);
            Element field = fieldData.getElement(0);
            if (!DATE.equals(field.name())) {
                throw new AssertionError("Date field is supposed to be first but got: " + field.name());
            }
            DateTime date = BB_RESULT_DATE_FORMATTER.parseDateTime(field.getValueAsString());

            for (int j = 1; j < fieldData.numElements(); j++) {
                field = fieldData.getElement(j);
                addField(date, security, field);
            }
        }
    }
}
