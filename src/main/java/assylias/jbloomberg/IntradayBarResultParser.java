/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package assylias.jbloomberg;

import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Name;
import java.util.HashSet;
import java.util.Set;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * A ResultParser to parse the responses received from the Bloomberg Session when sending a Historical Data request.
 *
 * This implementation is thread safe as the Bloomberg API might send results through more than one thread.
 */
final class IntradayBarResultParser extends AbstractResultParser {

    private final static Logger logger = LoggerFactory.getLogger(IntradayBarResultParser.class);
    /**
     * The various element names
     */
    private static final Name BAR_DATA = new Name("barData");
    private static final Name BAR_TICK_DATA = new Name("barTickData");
    private static final Name DATE = new Name("date");

    private final String security;

    /**
     * @param security the Bloomberg identifier of the security
     */
    public IntradayBarResultParser(String security) {
        this.security = security;
    }

    @Override
    protected void addFieldError(String field) {
        throw new UnsupportedOperationException("Intraday Bar Requests can't report a field exception");
    }

    /**
     * Only the fields we are interested in - the numEvents and value fields will be discarded
     */
    private static enum BarTickDataElements {

        TIME("time"),
        OPEN("open"),
        HIGH("high"),
        LOW("low"),
        CLOSE("close"),
        VOLUME("volume");
        private final Name elementName;
        private static final Set<Name> retainedNames = new HashSet<>(values().length, 1);

        static {
            for (BarTickDataElements e : values()) {
                retainedNames.add(e.asName());
            }
        }

        private static boolean retainField(Element field) {
            return retainedNames.contains(field.name());
        }

        private BarTickDataElements(String elementName) {
            this.elementName = new Name(elementName);
        }

        private Name asName() {
            return elementName;
        }
    }

    @Override
    protected void parseResponseNoResponseError(Element response) {
        if (response.hasElement(BAR_DATA, true)) {
            Element barData = response.getElement(BAR_DATA);
            parseBarData(barData);
        }
    }

    private void parseBarData(Element barData) {
        if (barData.hasElement(BAR_TICK_DATA, true)) {
            Element barTickDataArray = barData.getElement(BAR_TICK_DATA);
            parseBarTickDataArray(barTickDataArray);
        }
    }

    /**
     * There should be no more error at this point and we can happily parse the interesting portion of the response
     *
     */
    private void parseBarTickDataArray(Element barTickDataArray) {
        int countData = barTickDataArray.numValues();
        for (int i = 0; i < countData; i++) {
            Element fieldData = barTickDataArray.getValueAsElement(i);
            Element field = fieldData.getElement(0);
            if (!BarTickDataElements.TIME.asName().equals(field.name())) {
                throw new AssertionError("Time field is supposed to be first but got: " + field.name());
            }
            DateTime date = BB_RESULT_DATE_TIME_FORMATTER.parseDateTime(field.getValueAsString());

            for (int j = 1; j < fieldData.numElements(); j++) {
                field = fieldData.getElement(j);
                if (BarTickDataElements.retainField(field)) {
                    addField(date, security, field);
                }
            }
        }
    }
}
