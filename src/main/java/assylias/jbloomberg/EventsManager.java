/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package assylias.jbloomberg;

import com.bloomberglp.blpapi.CorrelationID;

/**
 * An interface defining the API for classes that receive subscription data and inform listeners. Several approaches can
 * be used. Typically it can be desirable that the implementation does not inform listeners about events they are not
 * interested in or does not send the same event twice.
 */
interface EventsManager {

    /**
     * Adds a listener that will be informed of data changes for the specific correlation id and field. The ticker is
     * only provided for easier processing but is not used as a filter.
     */
    void addEventListener(String ticker, CorrelationID id, RealtimeField field, DataChangeListener lst);

    /**
     * Informs the EventsManager that a new value has been received for the given correlation id and field
     */
    void fireEvent(CorrelationID id, RealtimeField rtField, Object value);
}
