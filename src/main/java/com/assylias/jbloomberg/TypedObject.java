/*
 * Copyright (C) 2013 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Yann Le Tallec
 */
public class TypedObject {

    private final Object o;

    private TypedObject(Object o) {
        this.o = Preconditions.checkNotNull(o, "o can't be null");
    }

    public static TypedObject of(Object o) {
        return new TypedObject(o);
    }

    public boolean asBoolean() {
        return (boolean) o;
    }

    public int asInt() {
        return ((Number) o).intValue();
    }

    public double asDouble() {
        return ((Number) o).doubleValue();
    }

    public String asString() {
        return o.toString();
    }

    public boolean isList() {
        return o instanceof List;
    }
    
    @SuppressWarnings("unchecked")
    public List<TypedObject> asList() {
        return (List<TypedObject>) o;
    }
    
    @SuppressWarnings("unchecked")
    public <T> List<T> asList(Class<T> clazz) {
        return (List<T>) o;
    }

    public <T> T as(Class<T> clazz) {
        return clazz.cast(o);
    }

    public Object get() {
        return o;
    }

    @Override
    public String toString() {
        return o.toString();
    }
    /**
     * 
     * @return the hashcode of the underlying object
     */
    @Override
    public int hashCode() {
        return o.hashCode();
    }

    /**
     * @return true if the underlying objects are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) return false;
        final TypedObject other = (TypedObject) obj;
        return o.equals(other.o);
    }
}
