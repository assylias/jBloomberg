/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package assylias.jbloomberg;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import java.util.Map;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that represents the result returned by a Bloomberg IntradayBarData request.
 * This implementation uses guava's Tables which are a good fit for the type of structure returned by historical/static
 * data requests (guava's Tables can be thought of as Excel spreadsheets with rows and columns).
 * <br>
 * To continue the analogy with Excel, the data is stored in a sheet which contains one row per date and one column per
 * field.
 * <br>
 * Convenience methods are provided to access one specific rows / columns. Those methods return immutable copies of the
 * underlying rows / columns.
 * <br>
 * Finally, the object returned from the cell's getters (i.e. a combination of a date / field) are either boxed
 * primitives or Strings. So if a query is supposed to return a double for example, it is normally safe to assume
 * that the returned Object is in fact a Double and that the Object can be cast to a double.
 * <br>
 * This class is thread safe by being synchronized. That would not scale very well under high contention but that is an
 * unlikely use case.
 */
public class IntradayBarData extends AbstractRequestResult {

    private final static Logger logger = LoggerFactory.getLogger(IntradayBarData.class);

    /**
     * a Table of date / field / value, which contains one row per date, one column per field.
     */
    private final Table<DateTime, IntradayBarField, Object> data = TreeBasedTable.create();

    /**
     * IntradayBar only return one security's data - this is the security
     */
    private final String security;

    public IntradayBarData(String security) {
        this.security = security;
    }

    @Override
    public synchronized boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public synchronized String toString() {
        StringBuilder sb = new StringBuilder("[DATA]");
        if (isEmpty()) {
            sb.append("{}");
        } else {
            sb.append("{").append(data).append("}");
        }
        if (!getSecurityErrors().isEmpty()) {
            sb.append("[SECURITY_ERRORS]").append(getSecurityErrors());
        }
        if (!getFieldErrors().isEmpty()) {
            sb.append("[FIELD_EXCEPTIONS]").append(getFieldErrors());
        }
        return sb.toString();
    }

    /**
     * Adds a value to the HistoricalData structure for that security / field / date combination.
     */
    @Override
    synchronized void add(DateTime date, String field, Object value) {
        try {
            data.put(date, IntradayBarField.of(field), value);
        } catch (IllegalArgumentException e) {
            logger.debug("{} - {}", e.getMessage(), value);
        }
    }

    /**
     *
     * @return the security for which the intraday data has been retrieved
     */
    public String getSecurity() {
        return security;
    }

    /**
     * Adds a filter on a specific field (column)
     */
    public ResultForField forField(IntradayBarField field) {
        return new ResultForField(field);
    }

    /**
     * Adds a filter on a specific date (row)
     */
    public ResultForDate forDate(DateTime date) {
        return new ResultForDate(date);
    }

    /**
     * The table contains one security per row, one field per column and the objects are the values of each combination
     * of security ID / field. Both the rows and columns are sorted in alphabetical order.
     *
     * @return an immutable copy of the whole table - the table can be empty
     */
    public Table<DateTime, IntradayBarField, Object> get() {
        return ImmutableTable.copyOf(data);
    }

    public class ResultForDate {

        private final DateTime date;

        private ResultForDate(DateTime date) { //not for public use
            this.date = date;
        }

        /**
         * @return the value for the selected field / date combination or null if there is no value in
         *         that cell
         */
        public Object forField(IntradayBarField field) {
            return data.get(date, field);
        }

        /**
         * The returned map contains fields as keys and field's value for the selected date as values.
         *
         * @return an immutable copy of the map corresponding to the security - the map can be empty
         */
        public Map<IntradayBarField, Object> get() {
            return ImmutableMap.copyOf(data.row(date));
        }
    }

    public class ResultForField {

        private final IntradayBarField field;

        private ResultForField(IntradayBarField field) { //not for public use
            this.field = field;
        }

        /**
         * @return the value for the selected field / date combination or null if there is no value in
         *         that cell
         */
        public Object forDate(DateTime date) {
            return data.get(date, field);
        }

        /**
         * The returned map contains dates as keys and the selected field's value as values.
         *
         * @return an immutable copy of the map corresponding to the fields - the map can be empty
         */
        public Map<DateTime, Object> get() {
            return ImmutableMap.copyOf(data.column(field));
        }
    }
}
