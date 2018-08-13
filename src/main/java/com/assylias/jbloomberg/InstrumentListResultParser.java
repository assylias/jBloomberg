package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Name;

public class InstrumentListResultParser extends AbstractResultParser<InstrumentList> {
    private static final Name RESULTS = Name.getName("results");
    private static final Name DESCRIPTION = Name.getName("description");

    @Override
    protected InstrumentList getRequestResult() {
        return new InstrumentList();
    }

    @Override
    protected void parseResponseNoResponseError(Element response) {
        if (response.hasElement(RESULTS)) {
            Element results = response.getElement(RESULTS);
            int numResults = results.numValues();
            for (int i = 0; i < numResults; i++) {
                Element result = results.getValueAsElement(i);
                parseSecurityData(result);
            }
        }
    }
}
