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
    protected void parseResponseNoResponseError(final Element response) {
        if (response.hasElement(RESULTS)) {
            final Element results = response.getElement(RESULTS);
            final int numResults = results.numValues();
            for (int i = 0; i < numResults; i++) {
                final Element result = results.getValueAsElement(i);
                parseSecurityData(result);
            }
        }
    }
}
