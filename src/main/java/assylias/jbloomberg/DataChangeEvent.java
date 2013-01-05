/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package assylias.jbloomberg;

/**
 * A DataChangeEvent gets delivered whenever a real time subscription sends a new value for a tracked data.<br>
 * This class is immutable and thread safe.
 * <p/>
 * Normally DataChangeEvents are accompanied by the name and the old and new value of the changed data. If the new value
 * is a primitive type (such as int or boolean) it must be boxed as an Object (such as Integer or Boolean).
 * <p/>
 * Null values may be provided for the old and the new values if their true values are not known.
 * <p/>
 * The event source is a String containing the ID of the security, typically a ticker.
 */
public final class DataChangeEvent {

    /**
     * *
     * Previous value for that data field. May be null if not known.
     * <p/>
     * @serial
     */
    private final String sourceString;
    /**
     * *
     * Name of the data that changed. Cannot be null.
     * <p/>
     * @serial
     */
    private final String dataName;
    /**
     * *
     * New value for that data field. May be null if not known.
     * <p/>
     * @serial
     */
    private final Object newValue;
    /**
     * *
     * Previous value for that data field. May be null if not known.
     * <p/>
     * @serial
     */
    private final Object oldValue;

    /**
     * *
     * Constructs a new DataChangeEvent.
     * <p/>
     * @param source   The ID of the security that fired the event.
     * @param dataName The programmatic name of the data that was changed.
     * @param oldValue The old value of the property.
     * @param newValue The new value of the property.
     */
    public DataChangeEvent(String source, String dataName, Object oldValue, Object newValue) {
//        super(source);
        this.sourceString = source;
        this.dataName = dataName;
        this.newValue = newValue;
        this.oldValue = oldValue;
    }

    /**
     *
     * @return the ID of the security that fired the event
     */
    public String getSource() {
        return sourceString;
    }

    /**
     * @return The programmatic name of the data that was changed, typically a specific field.
     */
    public String getDataName() {
        return dataName;
    }

    /**
     * *
     * Gets the new value for the data, expressed as an Object.
     * <p/>
     * @return The new value for the data, expressed as an Object. May be null if unknown.
     */
    public Object getNewValue() {
        return newValue;
    }

    /**
     * *
     * Gets the old value for the data, expressed as an Object.
     * <p/>
     * @return The old value for the data, expressed as an Object. May be null if unknown.
     */
    public Object getOldValue() {
        return oldValue;
    }

    /**
     *
     * @return A description of the DataChangeEvent in the form: IBM US Equity,LAST_PRICE: 50.10==>50.15
     */
    @Override
    public String toString() {
        return "DataChangeEvent{" + sourceString + "," + dataName + ": " + oldValue + "==>" + newValue + "}";
    }
}
