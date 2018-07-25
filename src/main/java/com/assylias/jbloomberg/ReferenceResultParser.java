/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * A ResultParser to parse the responses received from the Bloomberg Session when sending a Reference Data request.
 *
 * This implementation is thread safe as the Bloomberg API might send results through more than one thread.
 */
final class ReferenceResultParser extends AbstractResultParser<ReferenceData> {
    public ReferenceResultParser() {
        super((res, response) -> {
            if (response.hasElement(SECURITY_DATA, true)) {
                Element securityDataArray = response.getElement(SECURITY_DATA);
                int numSecurities = securityDataArray.numValues();
                for (int k = 0; k < numSecurities; k++) {
                    Element securityData = securityDataArray.getValueAsElement(k);
                    parseSecurityData(res, securityData, (security, fieldDataArray) -> {
                        // There should be no more error at this point and we can happily parse the interesting portion of the response
                        final int numberOfFields = fieldDataArray.numElements();
                        for (int i = 0; i < numberOfFields; i++) {
                            final Element field = fieldDataArray.getElement(i);
                            res.add(security, field.name().toString(), BloombergUtils.getSpecificObjectOf(field));
                        }
                    });
                }
            }
        });
    }

    @Override
    protected ReferenceData getRequestResult() {
        return new ReferenceData();
    }
}
