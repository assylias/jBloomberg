/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package assylias.jbloomberg;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.joda.time.DateTime;

/**
 * A class that represents the result returned by a Bloomberg request (historical, intraday, static or portfolio
 * requests).
 * This implementation uses guava's Tables which are a good fit for the type of structure returned by historical/static
 * data requests (guava's Tables can be thought of as Excel spreadsheets with rows and columns).
 * <br>
 * To continue the analogy with Excel, the data for a specific security is stored on a specific sheet and each sheet
 * contains one row per date and one column per field.
 * <br>
 * Convenience methods are provided to access one specific sheet / row / column. Those methods return immutable copies
 * of the underlying tables.
 * <br>
 * If some securities / fields included in the original request were invalid and returned errors, this can be queried
 * via the ad hoc error checking methods. When querying data on those securities / fields, the returned value will be
 * null.
 * <br>
 * Finally, the object returned from the cell's getters (i.e. a combination of a field / security / date) are either
 * boxed primitives or Strings. So if a query is supposed to return a double for example, it is normally safe to assume
 * that the returned Object is in fact a Double and that the Object can be cast to a double.
 * <br>
 * This class is thread safe by being synchronized. That would not scale very well under high contention but that is an
 * unlikely use case.
 */
public final class RequestResult {

    /**
     * A set of security identifiers that returned error messages - can be empty
     */
    private final Set<String> securityErrors = new HashSet<>();
    /**
     * A set of fields that returned error messages - can be empty
     */
    private final Set<String> fieldErrors = new HashSet<>();
    /**
     * a Map of ticker / table. Each table contains one row per date, one column per field.
     */
    private final Map<String, Table<DateTime, String, Object>> data = new HashMap<>();

    /**
     * Even if this method returns true, the request might have returned valid data.
     * For example, if a request was sent for 2 tickers and one was invalid, there will be an error corresponding to the
     * invalid ticker but some data might have been retrieved for the valid ticker.
     *
     * @return true if the request resulted in one or more errors.
     */
    public synchronized boolean hasErrors() {
        return !(securityErrors.isEmpty() && fieldErrors.isEmpty());
    }

    /**
     * @return the fields that returned an error. The returned set can be empty.
     */
    public synchronized Set<String> getFieldErrors() {
        return fieldErrors;
    }

    /**
     * @return the security identifiers that returned an error. The returned set can be empty.
     */
    public synchronized Set<String> getSecurityErrors() {
        return securityErrors;
    }

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
    public synchronized boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public synchronized String toString() {
        StringBuilder sb = new StringBuilder("[DATA]");
        if (isEmpty()) {
            sb.append("{}");
        } else {
            sb.append("{");
            for (Map.Entry<String, Table<DateTime, String, Object>> e : data.entrySet()) {
                sb.append("[").append(e.getKey()).append("]");
                sb.append(e.getValue());
            }
            sb.append("}");
        }
        if (!securityErrors.isEmpty()) {
            sb.append("[SECURITY_ERRORS]").append(securityErrors);
        }
        if (!fieldErrors.isEmpty()) {
            sb.append("[FIELD_EXCEPTIONS]").append(fieldErrors);
        }
        return sb.toString();
    }

    synchronized void addSecurityError(String security) {
        securityErrors.add(security);
    }

    synchronized void addFieldError(String field) {
        fieldErrors.add(field);
    }

    /**
     * Adds a value to the HistoricalData structure for that security / field / date combination.
     */
    synchronized void add(DateTime date, String security, String field, Object value) {
        Table<DateTime, String, Object> securityTable = data.get(security);
        if (securityTable == null) {
            securityTable = TreeBasedTable.create(); //to have the dates in order
            data.put(security, securityTable);
        }
        securityTable.put(date, field, value);
    }

    /**
     * Method to build queries in order to retrieve a specific table, column, row or cell.
     *
     * @param security the security for which data is required
     *
     * @return a query builder to build the query.
     */
    public synchronized ResultForSecurity forSecurity(String security) {
        return new ResultForSecurity(data.get(security));
    }

    /**
     * Used to filter the result of a request by security, field and date.
     */
    public static class ResultForSecurity {

        /**
         * The table corresponding to the selected security
         */
        private final Table<DateTime, String, Object> securityTable;

        private ResultForSecurity(Table<DateTime, String, Object> securityTable) { //not for public use
            this.securityTable = securityTable;
        }

        /**
         * Adds a filter on a specific field (column)
         */
        public ResultForSecurityAndField forField(String field) {
            return new ResultForSecurityAndField(field);
        }

        /**
         * Adds a filter on a specific date (row)
         */
        public ResultForSecurityAndDate forDate(DateTime date) {
            return new ResultForSecurityAndDate(date);
        }

        /**
         * @return an immutable copy of the table for the specified security - the table can be empty
         */
        public Table<DateTime, String, Object> get() {
            return securityTable == null ? ImmutableTable.<DateTime, String, Object>of() : ImmutableTable.copyOf(securityTable);
        }

        public class ResultForSecurityAndField {

            private final String field;

            private ResultForSecurityAndField(String field) { //not for public use
                this.field = field;
            }

            /**
             * @return the value for the selected field / security / date combination or null if there is no value in
             *         that cell
             */
            public Object forDate(DateTime date) {
                return securityTable == null ? null : securityTable.get(date, field);
            }

            /**
             * @return an immutable copy of the map corresponding to the security / field column - the map can be empty
             */
            public Map<DateTime, Object> get() {
                return securityTable == null ? Collections.EMPTY_MAP : ImmutableMap.copyOf(securityTable.column(field));
            }
        }

        public class ResultForSecurityAndDate {

            private final DateTime date;

            private ResultForSecurityAndDate(DateTime date) { //not for public use
                this.date = date;
            }

            /**
             * @return the value for the selected security / field / date combination or null if there is no value was
             *         found in that cell
             */
            public Object forField(String field) {
                return securityTable == null ? null : securityTable.get(date, field);
            }

            /**
             * @return a map corresponding to the security / date row - the map can be empty
             */
            public Map<String, Object> get() {
                return securityTable == null ? Collections.EMPTY_MAP : ImmutableMap.copyOf(securityTable.row(date));
            }
        }
    }
}
