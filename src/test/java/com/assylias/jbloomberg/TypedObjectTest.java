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

import com.assylias.jbloomberg.TypedObject.TypeReference;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class TypedObjectTest {

  @Test(expectedExceptions = NullPointerException.class)
  public void noNull() {
    TypedObject.of(null);
  }

  @Test public void asInt() {
    assertEquals(TypedObject.of(123).asInt(), 123);
  }

  @Test public void asIntNumber() {
    assertEquals(TypedObject.of(123.456d).asInt(), 123);
    assertEquals(TypedObject.of(BigDecimal.TEN).asInt(), 10);
  }

  @Test(expectedExceptions = ClassCastException.class)
  public void asIntError() {
    TypedObject.of("asd").asInt();
  }

  @Test public void asDouble() {
    assertEquals(TypedObject.of(123.456d).asDouble(), 123.456d);
  }

  @Test public void asDoubleNumber() {
    assertEquals(TypedObject.of(123).asDouble(), 123d);
    assertEquals(TypedObject.of(BigDecimal.TEN).asDouble(), 10d);
  }

  @Test(expectedExceptions = ClassCastException.class)
  public void asDoubleError() {
    TypedObject.of("asd").asInt();
  }

  @Test public void asString() {
    assertEquals(TypedObject.of("123").asString(), "123");
    assertEquals(TypedObject.of(123).asString(), "123");
    assertEquals(TypedObject.of(123d).asString(), "123.0");
    assertEquals(TypedObject.of("123").toString(), "123");
    assertEquals(TypedObject.of(123).toString(), "123");
    assertEquals(TypedObject.of(123d).toString(), "123.0");
  }

  @Test public void asList() {
    List<TypedObject> list = Arrays.asList(TypedObject.of("a"), TypedObject.of("b"));
    TypedObject o = TypedObject.of(list);
    assertEquals(o.asList().get(0).asString(), "a");
    assertTrue(o.isList());
  }

  @Test public void asMap() {
    Map<String, TypedObject> map = new HashMap<> ();
    map.put("a", TypedObject.of("b"));
    TypedObject o = TypedObject.of(map);
    assertEquals(o.asMap().get("a").asString(), "b");
    assertTrue(o.isMap());
  }

  @Test public void asListClass() {
    List<String> list = Arrays.asList("a", "b");
    TypedObject o = TypedObject.of(list);
    assertEquals(o.asList(String.class).get(0), "a");
    assertTrue(o.isList());
  }

  @Test public void asMapClass() {
    Map<String, Double> map = new HashMap<>();
    map.put("a", 1d);
    TypedObject o = TypedObject.of(map);
    assertEquals(o.asMap(Double.class).get("a"), 1d);
    assertTrue(o.isMap());
  }

  @Test public void asGenericType() {
    List<List<String>> list = Arrays.asList(Arrays.asList("a", "b"), Arrays.asList("c", "d"));
    TypedObject o = TypedObject.of(list);
    List<List<String>> result = o.as(new TypeReference<List<List<String>>>() {});
    assertEquals(result.get(0).get(0), "a");
    assertTrue(o.isList());
  }

  @Test(expectedExceptions = ClassCastException.class) public void asGenericType_exception() {
    List<List<String>> list = Arrays.asList(Arrays.asList("a", "b"), Arrays.asList("c", "d"));
    TypedObject o = TypedObject.of(list);
    List<List<Integer>> result = o.as(new TypeReference<List<List<Integer>>> () {});
    Integer i = result.get(0).get(0);
  }

  @Test public void hashcode() {
    Object o = new Object();
    assertEquals(TypedObject.of(o).hashCode(), o.hashCode());
  }

  @Test public void equals() {
    Object o = new Object();
    assertTrue(TypedObject.of(o).equals(TypedObject.of(o)));
    assertNotNull(TypedObject.of(o).equals(null)); //verify that equals checks for null
  }
}
