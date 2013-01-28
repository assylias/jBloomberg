/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package assylias.jbloomberg;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that represents the result returned by a Bloomberg IntradayTickData request.
 * This implementation uses guava's Tables which are a good fit for the type of structure returned by historical/static
 * data requests (guava's Tables can be thought of as Excel spreadsheets with rows and columns).
 * <br>
 * To continue the analogy with Excel, the data is stored in a sheet which contains one row per date and one column per
 * field. However, because several ticks can happen at the same time, each cell can contain one or more data.
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
public class IntradayTickData extends AbstractRequestResult {

    private final static Logger logger = LoggerFactory.getLogger(IntradayTickData.class);
    /**
     * a Table of date / field / value, which contains one row per date, one column per field.
     * The values are either a single value or a list of values if there was more than one tick for that datetime.
     */
    private final Table<DateTime, IntradayTickField, Object> data = TreeBasedTable.create();
    /**
     * IntradayBar only return one security's data - this is the security
     */
    private final String security;

    public IntradayTickData(String security) {
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
            IntradayTickField f = IntradayTickField.of(field);
            Object previousValue = data.get(date, f);
            if (previousValue instanceof Collection) { //already several values in a list a list in there
                ((Collection) previousValue).add(value);
            } else if (previousValue != null) { //already one value: create a list of values
                List<Object> list = new ArrayList<> ();
                list.add(previousValue);
                list.add(value);
                data.put(date, f, list);
            } else { //new value, just add it
                data.put(date, f, value);
            }
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
     *
     * @param field the field for which the data is needed
     * @return a multimap that can contain one or more values per date.
     */
    public Multimap<DateTime, Object> forField(IntradayTickField field) {
        Map<DateTime, Object> fieldData = data.column(field);
        LinkedListMultimap<DateTime, Object> list = LinkedListMultimap.create(fieldData.size());
        for (Map.Entry<DateTime, Object> e : fieldData.entrySet()) {
            Object v = e.getValue();
            if (v instanceof Collection) {
                for (Object value : (Collection) v) {
                    list.put(e.getKey(), value);
                }
            } else {
                list.put(e.getKey(), v);
            }
        }

        return list;
    }
}
