package com.assylias.jbloomberg;

import java.util.List;

/**
 * A DataChangeMultiListener is passed to a BloombergSession to be informed of real time changes to a combination of
 * securities / fields. Updates are sent to the listener in the form of {@link List<DataChangeEvent>} objects
 * representing all fields recieved in a given message.
 */
public interface DataChangeMultiListener {
    /**
     * Invoked when a change occurs in the DataFeed.
     */
    void dataChanged(List<DataChangeEvent> es);
}
