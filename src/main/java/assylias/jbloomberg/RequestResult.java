/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package assylias.jbloomberg;

import java.util.Set;

/**
 * An interface that represents the result returned by a Bloomberg request (historical, intraday, static or portfolio
 * requests).
 *
 * All implementations are thread safe.
 */
public interface RequestResult {

    /**
     * Even if this method returns true, the request might have returned valid data.
     * For example, if a request was sent for 2 tickers and one was invalid, there will be an error corresponding to the
     * invalid ticker but some data might have been retrieved for the valid ticker.
     *
     * @return true if the request resulted in one or more errors.
     */
    boolean hasErrors();

    /**
     * @return the fields that returned an error. The returned set can be empty.
     */
    Set<String> getFieldErrors();

    /**
     * @return the security identifiers that returned an error. The returned set can be empty.
     */
    Set<String> getSecurityErrors();

    /**
     * The main reasons why this might return true:
     * <ul>
     * <li> the structure has not been populated yet
     * <li> all the information requested returned errors (none of the tickers and/or fields was valid)
     * <li> the date range was invalid
     * <li> no data was returned because there was no data available for that ticker/field/date range combination
     * </ul>
     *
     * @return true if no data was retrieved from Bloomberg.
     */
    boolean isEmpty();
}
