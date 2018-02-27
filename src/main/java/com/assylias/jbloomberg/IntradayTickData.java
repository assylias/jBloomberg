/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.google.common.collect.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that represents the result returned by a Bloomberg IntradayTickData request.
 * Note: the LocalDateTime objects are based on the UTC timezone. For other timezones the calling code needs to apply the relevant timezone conversions.<br>
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
    private final Table<OffsetDateTime, IntradayTickField, TypedObject> data = TreeBasedTable.create();
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
    synchronized void add(OffsetDateTime date, String field, Object value) {
        try {
            IntradayTickField f = IntradayTickField.of(field);
            TypedObject previousValue = data.get(date, f);
            TypedObject newValue = TypedObject.of(value);
            if (previousValue == null) { //new value, just add it
                data.put(date, f, newValue);
            } else if (previousValue.isList()) { //already several values in a list - add the new value to the list
                previousValue.asList().add(newValue);
            } else { //already one value: create a list of values
                List<TypedObject> list = new ArrayList<> ();
                list.add(previousValue);
                list.add(newValue);
                data.put(date, f, TypedObject.of(list));
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

    public Table<OffsetDateTime, IntradayTickField, TypedObject> get() {
        return ImmutableTable.copyOf(data);
    }
    /**
     *
     * @param field the field for which the data is needed
     * @return a multimap that can contain one or more values per date.
     */
    public Multimap<OffsetDateTime, TypedObject> forField(IntradayTickField field) {
        Map<OffsetDateTime, TypedObject> fieldData = data.column(field);
        LinkedListMultimap<OffsetDateTime, TypedObject> multimap = LinkedListMultimap.create(fieldData.size());
        for (Map.Entry<OffsetDateTime, TypedObject> e : fieldData.entrySet()) {
            TypedObject v = e.getValue();
            if (v.isList()) {
                for (TypedObject value : v.asList()) {
                    multimap.put(e.getKey(), value);
                }
            } else {
                multimap.put(e.getKey(), v);
            }
        }

        return multimap;
    }
}
