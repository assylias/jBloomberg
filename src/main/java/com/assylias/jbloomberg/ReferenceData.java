/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import java.util.Map;

/**
 * A class that represents the result returned by a Bloomberg ReferenceData request.
 * This implementation uses guava's Tables which are a good fit for the type of structure returned by historical/static
 * data requests (guava's Tables can be thought of as Excel spreadsheets with rows and columns).
 * <br>
 * To continue the analogy with Excel, the data is stored in a sheet which contains one row per security and one column
 * per field.
 * <br>
 * Convenience methods are provided to access one specific rows / columns. Those methods return immutable copies of the
 * underlying rows / columns.
 * <br>
 * If some securities / fields included in the original request were invalid and returned errors, this can be queried
 * via the ad hoc error checking methods. When querying data on those securities / fields, the returned value will be
 * null.
 * <br>
 * Finally, the object returned from the cell's getters (i.e. a combination of a field / security / date) are either
 * boxed primitives or Strings. So if a query is supposed to return a double for example, it is normally safe to assume
 * that the returned Object is in fact a Double and that the Object can be cast to a double.
 * <br>
 * Note that <strong>if a field returns bulk data, the cell corresponding to that field will contain a List</strong>,
 * which
 * itself will contain either simple Objects (wrapped primitives or Strings) or Maps. For example, querying the field
 * <code>TOP_20_HOLDERS_PUBLIC_FILINGS</code> will return a List with 20 Maps of the form (for example):
 * <code>{Amount Held=1634951.0, Percent Outstanding=0.19, etc. }</code>.
 * <br>
 * <strong>This class is thread safe by being synchronized on <code>this</code></strong>. That would not scale very well under high contention but that is an
 * unlikely use case.
 */
public final class ReferenceData extends AbstractRequestResult {

    /**
     * a Table of ticker / field / value, which contains one row per security, one column per field.
     */
    private final Table<String, String, Object> data = TreeBasedTable.create();

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
    synchronized void add(String security, String field, Object value) {
        data.put(security, field, value);
    }

    /**
     * Adds a filter on a specific field (column)
     */
    public ResultForField forField(String field) {
        return new ResultForField(field);
    }

    /**
     * Adds a filter on a specific security (row)
     */
    public ResultForSecurity forSecurity(String security) {
        return new ResultForSecurity(security);
    }

    /**
     * The table contains one security per row, one field per column and the objects are the values of each combination
     * of security ID / field. Both the rows and columns are sorted in alphabetical order.
     *
     * @return an immutable copy of the whole table - the table can be empty
     */
    public Table<String, String, Object> get() {
        return ImmutableTable.copyOf(data);
    }

    public class ResultForSecurity {

        private final String security;

        private ResultForSecurity(String security) { //not for public use
            this.security = security;
        }

        /**
         * @return the value for the selected field / security combination or null if there is no value in
         *         that cell
         */
        public Object forField(String field) {
            return data.get(security, field);
        }

        /**
         * The returned map contains fields as keys and field's value for the selected security as values.
         *
         * @return an immutable copy of the map corresponding to the security - the map can be empty
         */
        public Map<String, Object> get() {
            return ImmutableMap.copyOf(data.row(security));
        }
    }

    public class ResultForField {

        private final String field;

        private ResultForField(String field) { //not for public use
            this.field = field;
        }

        /**
         * @return the value for the selected field / security combination or null if there is no value in
         *         that cell
         */
        public Object forSecurity(String security) {
            return data.get(security, field);
        }

        /**
         * The returned map contains security IDs as keys and the selected field's value as values.
         *
         * @return an immutable copy of the map corresponding to the fields - the map can be empty
         */
        public Map<String, Object> get() {
            return ImmutableMap.copyOf(data.column(field));
        }
    }
}
