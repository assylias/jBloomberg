package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Name;

public class InstrumentListResultParser extends AbstractResultParser<InstrumentList> {
    private static final Name RESULTS = Name.getName("results");
    private static final Name DESCRIPTION = Name.getName("description");

    public InstrumentListResultParser() {
        super((res, response) -> {
            if (response.hasElement(RESULTS, true)) {
                final Element results = response.getElement(RESULTS);
                final int numResults = results.numValues();
                for (int i = 0; i < numResults; i++) {
                    final Element result = results.getValueAsElement(i);
                    final String security = result.getElementAsString(SECURITY);
                    final String description = result.getElementAsString(DESCRIPTION);
                    res.add(security, description);
                }
            }
        });
    }

    @Override
    protected InstrumentList getRequestResult() {
        return new InstrumentList();
    }
}
