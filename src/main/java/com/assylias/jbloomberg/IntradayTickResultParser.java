/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import static com.assylias.jbloomberg.DateUtils.toOffsetDateTime;
import com.bloomberglp.blpapi.Datetime;
import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * A ResultParser to parse the responses received from the Bloomberg Session when sending a historical intraday tick
 * data request.
 *
 * This implementation is thread safe as the Bloomberg API might send results through more than one thread.
 */
final class IntradayTickResultParser extends AbstractResultParser<IntradayTickData> {
    /**
     * The various element names
     */
    private static final Name TICK_DATA = new Name("tickData");
    private final String security;

    /**
     * @param security the Bloomberg identifier of the security
     */
    public IntradayTickResultParser(final String security) {
        super((res, response) -> {
            if (response.hasElement(TICK_DATA, true)) {
                final Element barData = response.getElement(TICK_DATA);
                parseTickData(res, barData);
            }
        });
        this.security = security;
    }

    @Override
    protected IntradayTickData getRequestResult() {
        return new IntradayTickData(security);
    }

    /**
     * Only the fields we are interested in - the numEvents and value fields will be discarded
     */
    private enum TickDataElements {
        TIME("time"),
        TYPE("type"),
        VALUE("value"),
        SIZE("size"),
        CONDITION_CODE("conditionCode"),
        EXCHANGE_CODE("exchangeCode"),
        MIC_CODE("micCode"),
        BROKER_BUY_CODE("brokerBuyCode"),
        BROKER_SELL_CODE("brokerSellCode"),
        RPS_CODE("rpsCode");

        private final Name elementName;

        TickDataElements(String elementName) {
            this.elementName = new Name(elementName);
        }

        private Name asName() {
            return elementName;
        }
    }

    private static void parseTickData(final IntradayTickData result, final Element barData) {
        if (barData.hasElement(TICK_DATA, true)) {
            final Element tickDataArray = barData.getElement(TICK_DATA);
            parseTickDataArray(result, tickDataArray);
        }
    }

    /**
     * There should be no more error at this point and we can happily parse the interesting portion of the response
     */
    private static void parseTickDataArray(final IntradayTickData result, final Element tickDataArray) {
        final int countData = tickDataArray.numValues();
        for (int i = 0; i < countData; i++) {
            final Element fieldData = tickDataArray.getValueAsElement(i);
            final Element firstField = fieldData.getElement(0);
            if (!TickDataElements.TIME.asName().equals(firstField.name())) {
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
