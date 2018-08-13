/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * A ResultParser to parse the responses received from the Bloomberg Session when sending a Bloomeberg Search Data
 * request.
 *
 * This implementation is thread safe as the Bloomberg API might send results through more than one thread.
 */
final class BloombergSearchResultParser extends AbstractResultParser<BloombergSearchData> {

    private static final Logger logger = LoggerFactory.getLogger(BloombergSearchResultParser.class);
    private static final Name DATA_RECORDS = new Name("DataRecords");
    private static final Name DATA_FIELDS = new Name("DataFields");
    private static final Name STRING_VALUE = new Name("StringValue");
    @Override
    protected BloombergSearchData getRequestResult() {
        return new BloombergSearchData();
    }

    @Override
    protected void parseResponseNoResponseError(Element response) {
        if (response.hasElement(DATA_RECORDS, true)) {
            Element dataRecordsArray = response.getElement(DATA_RECORDS);
            parseDataRecordsArray(dataRecordsArray);
        }
    }

    private void parseDataRecordsArray(Element dataRecordsArray) {
        int numValues = dataRecordsArray.numValues();
        for (int i = 0; i < numValues; i++) {
            Element dataFields = dataRecordsArray.getValueAsElement(i);
            parseDataFieldsArray(dataFields);
        }
    }

    private void parseDataFieldsArray(Element dataFieldsArray) {
        int numValues = dataFieldsArray.numValues();
        if (dataFieldsArray.hasElement(DATA_FIELDS)) {
            Element dataFields = dataFieldsArray.getElement(DATA_FIELDS);
            for (int i = 0; i < numValues; i++) {
                Element dataField = dataFields.getValueAsElement(i);
                String stringValue = dataField.getElementAsString(STRING_VALUE);
                addSecurity(stringValue);
            }
        }
    }
}
