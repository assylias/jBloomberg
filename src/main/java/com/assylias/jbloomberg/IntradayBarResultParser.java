/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.Datetime;
import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Name;

import static com.assylias.jbloomberg.DateUtils.toOffsetDateTime;

/**
 *
 * A ResultParser to parse the responses received from the Bloomberg Session when sending a Historical Data request.
 *
 * This implementation is thread safe as the Bloomberg API might send results through more than one thread.
 */
final class IntradayBarResultParser extends AbstractResultParser<IntradayBarData> {
    /**
     * The various element names
     */
    private static final Name BAR_DATA = new Name("barData");
    private static final Name BAR_TICK_DATA = new Name("barTickData");

    private final String security;

    /**
     * @param security the Bloomberg identifier of the security
     */
    public IntradayBarResultParser(String security) {
        this.security = security;
    }

    @Override protected void parseResponseNoError(Element response, IntradayBarData result) {
        if (response.hasElement(BAR_DATA, true)) {
            Element barData = response.getElement(BAR_DATA);
            parseBarData(result, barData);
        }
    }

    @Override
    protected IntradayBarData getRequestResult() {
        return new IntradayBarData(security);
    }

    /**
     * Only the fields we are interested in - the numEvents and value fields will be discarded
     */
    private enum BarTickDataElements {
        TIME("time"),
        OPEN("open"),
        HIGH("high"),
        LOW("low"),
        CLOSE("close"),
        VOLUME("volume"),
        NUM_EVENTS("numEvents");

        private final Name elementName;

        BarTickDataElements(String elementName) {
            this.elementName = new Name(elementName);
        }

        private Name asName() {
            return elementName;
        }
    }

    private static void parseBarData(IntradayBarData result, Element barData) {
        if (barData.hasElement(BAR_TICK_DATA, true)) {
            final Element barTickDataArray = barData.getElement(BAR_TICK_DATA);
            parseBarTickDataArray(result, barTickDataArray);
        }
    }

    /**
     * There should be no more error at this point and we can happily parse the interesting portion of the response
     */
    private static void parseBarTickDataArray(IntradayBarData result, Element barTickDataArray) {
        final int countData = barTickDataArray.numValues();
        for (int i = 0; i < countData; i++) {
            final Element fieldData = barTickDataArray.getValueAsElement(i);
            final Element firstField = fieldData.getElement(0);
            if (!BarTickDataElements.TIME.asName().equals(firstField.name())) {
                throw new AssertionError("Time field is supposed to be first but got: " + firstField.name());
            }
            final Datetime dt = firstField.getValueAsDatetime();

            for (int j = 1; j < fieldData.numElements(); j++) {
                final Element field = fieldData.getElement(j);
                result.add(toOffsetDateTime(dt), field.name().toString(), BloombergUtils.getSpecificObjectOf(field));
            }
        }
    }
}