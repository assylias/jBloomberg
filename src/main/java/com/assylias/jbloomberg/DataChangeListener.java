/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

/**
 * A DataChangeListener is passed to a BloombergSession to be informed of real time changes to a combination of
 * securities / fields. Updates are sent to the listener in the form of DataChangeEvent objects.
 */
public interface DataChangeListener {

    /**
     * Invoked when a change occurs in the DataFeed.
     */
    void dataChanged(DataChangeEvent e);
}
