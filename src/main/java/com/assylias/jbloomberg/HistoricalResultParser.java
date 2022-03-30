/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.Element;
import com.google.common.annotations.VisibleForTesting;

import java.time.LocalDate;

/**
 * A ResultParser to parse the responses received from the Bloomberg Session when sending a Historical Data request.
 *
 * This implementation is thread safe as the Bloomberg API might send results through more than one thread.
 */
final class HistoricalResultParser extends AbstractResultParser<HistoricalData> {

    @Override protected void parseResponseNoError(Element response, HistoricalData result) {
        if (response.hasElement(SECURITY_DATA, true)) {
            final Element securityData = response.getElement(SECURITY_DATA);
            parseSecurityData(securityData, HistoricalResultParser::parse, result);
        }
    }

    private static void parse(Element data, String security, HistoricalData result) {
        // There should be no more error at this point and we can happily parse the interesting portion of the response
        final int countData = data.numValues();
        for (int i = 0; i < countData; i++) {
            final Element fieldData = data.getValueAsElement(i);
            final Element firstField = fieldData.getElement(0);
            if (!DATE.equals(firstField.name())) {
                throw new AssertionError("Date field is supposed to be first but got: " + firstField.name());
            }
            final LocalDate date = parseLocalDate(firstField.getValueAsString());

            int numElements = fieldData.numElements();
            for (int j = 1; j < numElements; j++) {
                final Element field = fieldData.getElement(j);
                result.add(date, security, field.name().toString(), BloombergUtils.getSpecificObjectOf(field));
            }
        }
    }

    @VisibleForTesting
    static LocalDate parseLocalDate(String bbDate) {
        return BB_RESULT_DATE_FORMATTER.parse(bbDate, LocalDate::from);
    }

    @Override
    protected HistoricalData getRequestResult() {
        return new HistoricalData();
    }
}
