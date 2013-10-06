/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Name;
import java.time.LocalDateTime;
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

    private final static Logger logger = LoggerFactory.getLogger(IntradayTickResultParser.class);
    /**
     * The various element names
     */
    private static final Name TICK_DATA = new Name("tickData");
    private final String security;

    /**
     * @param security the Bloomberg identifier of the security
     */
    public IntradayTickResultParser(String security) {
        this.security = security;
    }

    @Override
    protected void addFieldError(String field) {
        throw new UnsupportedOperationException("Intraday Tick Requests can't report a field exception");
    }

    @Override
    protected IntradayTickData getRequestResult() {
        return new IntradayTickData(security);
    }

    /**
     * Only the fields we are interested in - the numEvents and value fields will be discarded
     */
    private static enum TickDataElements {

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

        private TickDataElements(String elementName) {
            this.elementName = new Name(elementName);
        }

        private Name asName() {
            return elementName;
        }
    }

    @Override
    protected void parseResponseNoResponseError(Element response) {
        if (response.hasElement(TICK_DATA, true)) {
            Element barData = response.getElement(TICK_DATA);
            parseTickData(barData);
        }
    }

    private void parseTickData(Element barData) {
        if (barData.hasElement(TICK_DATA, true)) {
            Element tickDataArray = barData.getElement(TICK_DATA);
            parseTickDataArray(tickDataArray);
        }
    }

    /**
     * There should be no more error at this point and we can happily parse the interesting portion of the response
     *
     */
    private void parseTickDataArray(Element tickDataArray) {
        int countData = tickDataArray.numValues();
        for (int i = 0; i < countData; i++) {
            Element fieldData = tickDataArray.getValueAsElement(i);
            Element field = fieldData.getElement(0);
            if (!TickDataElements.TIME.asName().equals(field.name())) {
                throw new AssertionError("Time field is supposed to be first but got: " + field.name());
            }
            LocalDateTime date = BB_RESULT_DATE_TIME_FORMATTER.parse(field.getValueAsString(), LocalDateTime::from);

            for (int j = 1; j < fieldData.numElements(); j++) {
                field = fieldData.getElement(j);
                addField(date, field);
            }
        }
    }
}
