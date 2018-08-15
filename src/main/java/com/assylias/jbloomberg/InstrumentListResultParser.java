package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Name;

public class InstrumentListResultParser extends AbstractResultParser<InstrumentList> {
    private static final Name RESULTS = Name.getName("results");
    private static final Name DESCRIPTION = Name.getName("description");

    @Override protected void parseResponseNoError(Element response, InstrumentList result) {
        if (response.hasElement(RESULTS, true)) {
            final Element results = response.getElement(RESULTS);
            final int numResults = results.numValues();
            for (int i = 0; i < numResults; i++) {
                final Element value = results.getValueAsElement(i);
                final String security = value.getElementAsString(SECURITY);
                final String description = value.getElementAsString(DESCRIPTION);
                result.add(security, description);
            }
        }
    }

    protected InstrumentList getRequestResult() {
        return new InstrumentList();
    }
}
