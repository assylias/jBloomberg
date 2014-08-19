/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.assylias.fund.TypedObject;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A class that represents the result returned by a Bloomberg HistoricalData request.
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
public final class HistoricalData extends AbstractRequestResult {

    /**
     * a Map of ticker / table. Each table contains one row per date, one column per field.
     */
    private final Map<String, Table<LocalDate, String, TypedObject>> data = new HashMap<>();

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
            sb.append("{");
            for (Map.Entry<String, Table<LocalDate, String, TypedObject>> e : data.entrySet()) {
                sb.append("[").append(e.getKey()).append("]");
                sb.append(e.getValue());
            }
            sb.append("}");
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
    synchronized void add(LocalDate date, String security, String field, Object value) {
        Table<LocalDate, String, TypedObject> securityTable = data.get(security);
        if (securityTable == null) {
            securityTable = TreeBasedTable.create(); //to have the dates in order
            data.put(security, securityTable);
        }
        securityTable.put(date, field, TypedObject.of(value));
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
     * Method to build queries in order to retrieve a specific table, column, row or cell.
     *
     * @param field the field for which data is required
     *
     * @return a query builder to build the query.
     */
    public synchronized ResultForField forField(String field) {
        Table<LocalDate, String, TypedObject> forField = TreeBasedTable.create();
        for (Map.Entry<String, Table<LocalDate, String, TypedObject>> e : data.entrySet()) {
          String ticker = e.getKey();
          Map<LocalDate, TypedObject> fieldData = e.getValue().column(field);
          forField.column(ticker).putAll(fieldData);
        }
        return new ResultForField(forField);
    }

    /**
     * Returns the set of tickers held in this query result.
     *
     * @return the set of tickers held in this query result.
     */
    public synchronized Set<String> securities() {
        return data.keySet();
    }

    /**
     * Used to filter the result of a request by security, field and date.
     */
    public static class ResultForSecurity {

        /**
         * The table corresponding to the selected security
         */
        private final Table<LocalDate, String, TypedObject> securityTable;

        private ResultForSecurity(Table<LocalDate, String, TypedObject> securityTable) { //not for public use
            this.securityTable = securityTable;
        }

        /**
         * Adds a filter on a specific field (column)
         */
        public ResultForSecurityAndField forField(String field) {
            return new ResultForSecurityAndField(field, securityTable);
        }

        /**
         * Adds a filter on a specific date (row)
         */
        public ResultForSecurityAndDate forDate(LocalDate date) {
            return new ResultForSecurityAndDate(date, securityTable);
        }

        /**
         * @return an immutable copy of the table for the specified security - the table can be empty
         */
        public Table<LocalDate, String, TypedObject> get() {
            return securityTable == null ? ImmutableTable.<LocalDate, String, TypedObject>of() : ImmutableTable.copyOf(
                    securityTable);
        }
    }

    /**
     * Used to filter the result of a request by security, field and date.
     */
    public static class ResultForField {

        /**
         * The table corresponding to the selected security
         */
        private final Table<LocalDate, String, TypedObject> fieldTable;

        private ResultForField(Table<LocalDate, String, TypedObject> fieldTable) { //not for public use
            this.fieldTable = fieldTable;
        }

        /**
         * Adds a filter on a specific field (column)
         */
        public ResultForSecurityAndField forSecurity(String security) {
            return new ResultForSecurityAndField(security, fieldTable);
        }

        /**
         * @return an immutable copy of the table for the specified field - the table can be empty
         */
        public Table<LocalDate, String, TypedObject> get() {
            return fieldTable == null ? ImmutableTable.<LocalDate, String, TypedObject>of() : ImmutableTable.copyOf(
                    fieldTable);
        }
    }

    public static class ResultForSecurityAndField {

        private final String securityOrField;
        private final Table<LocalDate, String, TypedObject> table;

        private ResultForSecurityAndField(String securityOrField, Table<LocalDate, String, TypedObject> table) { //not for public use
            this.securityOrField = securityOrField;
            this.table = table;
        }

        /**
         * @return the value for the selected field / security / date combination or null if there is no value in
         *         that cell
         */
        public TypedObject forDate(LocalDate date) {
            return table == null ? null : table.get(date, securityOrField);
        }

        /**
         * @return an immutable copy of the map corresponding to the security / field column - the map can be empty
         */
        public Map<LocalDate, TypedObject> get() {
            return table == null ? Collections.<LocalDate, TypedObject>emptyMap()
                    : ImmutableMap.copyOf(table.column(securityOrField));
        }
    }

    public static class ResultForSecurityAndDate {

        private final LocalDate date;
        private final Table<LocalDate, String, TypedObject> securityTable;

        private ResultForSecurityAndDate(LocalDate date, Table<LocalDate, String, TypedObject> securityTable) { //not for public use
            this.date = date;
            this.securityTable = securityTable;
        }

        /**
         * @return the value for the selected security / field / date combination or null if there is no value was
         *         found in that cell
         */
        public TypedObject forField(String field) {
            return securityTable == null ? null : securityTable.get(date, field);
        }

        /**
         * @return a map corresponding to the security / date row - the map can be empty
         */
        public Map<String, TypedObject> get() {
            return securityTable == null ? Collections.<String, TypedObject>emptyMap()
                    : ImmutableMap.copyOf(securityTable.row(date));
        }
    }
}
