/*
 * Copyright 2014 Yann Le Tallec.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.assylias.jbloomberg;

import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * A simple wrapper that allows easy casting to common types. The object contained in an instance of TypedObject is
 * guaranteed to be non null.
 */
public class TypedObject {

  private final Object o;

  private TypedObject(Object o) {
    this.o = requireNonNull(o, "o can't be null");
  }

  /**
   * Returns a TypedObject that wraps the given object.
   *
   * @param o an object to be wrapped
   *
   * @return a TypedObject that wraps the given object.
   *
   * @throws NullPointerException if o is null
   */
  public static TypedObject of(Object o) {
    return new TypedObject(o);
  }

  public boolean asBoolean() {
    return ((Boolean) o).booleanValue();
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

  public boolean isMap() {
    return o instanceof Map;
  }

  @SuppressWarnings("unchecked")
  public List<TypedObject> asList() {
    return (List<TypedObject>) o;
  }

  @SuppressWarnings("unchecked")
  public Map<String, TypedObject> asMap() {
    return (Map<String, TypedObject>) o;
  }


  @SuppressWarnings("unchecked")
  public <T> List<T> asList(Class<T> clazz) {
    return (List<T>) o;
  }

  @SuppressWarnings("unchecked")
  public <T> Map<String, T> asMap(Class<T> clazz) {
    return (Map<String, T>) o;
  }


  public <T> T as(Class<T> clazz) {
    return clazz.cast(o);
  }

  /**
   *
   * @param <T> A generic type, for example {@code List<String>}
   * @param type an instance of a TypeReference applied to the generic type, typically: {@code new TypeReference<List<String>>() {}}
   * @return a generic instance of the wrapped object.
   */
  @SuppressWarnings("unchecked")
  public <T> T as(TypeReference<T> type) {
    Class<?> c = TypeResolver.resolveRawArgument(TypeReference.class, type.getClass());
    return (T) c.cast(o);
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

  public static abstract class TypeReference<T> {
  }
}
