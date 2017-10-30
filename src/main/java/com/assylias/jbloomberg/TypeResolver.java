/*
 * Copyright 2017 Yann Le Tallec.
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

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is a trimmed-down version of Jonathan Halterman's excellent TypeResolver class: https://github.com/jhalterman/typetools
 */
@SuppressWarnings("restriction")
final class TypeResolver {

  /**
   * An unknown type.
   */
  public static final class Unknown {
    private Unknown() {
    }
  }

  private TypeResolver() {
  }

  static <T, S extends T> Class<?> resolveRawArgument(Class<T> type, Class<S> subType) {
    return resolveRawArgument(resolveGenericType(type, subType), subType);
  }

  private static Class<?> resolveRawArgument(Type genericType, Class<?> subType) {
    Class<?>[] arguments = resolveRawArguments(genericType, subType);
    if (arguments == null)
      throw new IllegalArgumentException("Could not resolve a generic class: " + genericType);

    if (arguments.length != 1)
      throw new IllegalArgumentException("Expected 1 argument for generic type " + genericType + " but found " + arguments.length);

    return arguments[0];
  }

  private static Class<?>[] resolveRawArguments(Type genericType, Class<?> subType) {
    Class<?>[] result = null;

    if (genericType instanceof ParameterizedType) {
      ParameterizedType paramType = (ParameterizedType) genericType;
      Type[] arguments = paramType.getActualTypeArguments();
      result = new Class[arguments.length];
      for (int i = 0; i < arguments.length; i++) {
        result[i] = resolveRawClass(arguments[i], subType);
      }
    } else if (genericType instanceof TypeVariable) {
      result = new Class[1];
      result[0] = resolveRawClass(genericType, subType);
    } else if (genericType instanceof Class) {
      TypeVariable<?>[] typeParams = ((Class<?>) genericType).getTypeParameters();
      result = new Class[typeParams.length];
      for (int i = 0; i < typeParams.length; i++) {
        result[i] = resolveRawClass(typeParams[i], subType);
      }
    }

    return result;
  }

  private  static Type resolveGenericType(Class<?> type, Type subType) {
    Class<?> rawType;
    if (subType instanceof ParameterizedType)
      rawType = (Class<?>) ((ParameterizedType) subType).getRawType();
    else
      rawType = (Class<?>) subType;

    if (type.equals(rawType))
      return subType;

    Type result;
    if (type.isInterface()) {
      for (Type superInterface : rawType.getGenericInterfaces()) {
        if (superInterface != null && !superInterface.equals(Object.class))
          if ((result = resolveGenericType(type, superInterface)) != null)
            return result;
      }
    }

    Type superClass = rawType.getGenericSuperclass();
    if (superClass != null && !superClass.equals(Object.class))
      if ((result = resolveGenericType(type, superClass)) != null)
        return result;

    return null;
  }

  private static Class<?> resolveRawClass(Type genericType, Class<?> subType) {
    if (genericType instanceof Class) {
      return (Class<?>) genericType;
    } else if (genericType instanceof ParameterizedType) {
      return resolveRawClass(((ParameterizedType) genericType).getRawType(), subType);
    } else if (genericType instanceof GenericArrayType) {
      GenericArrayType arrayType = (GenericArrayType) genericType;
      Class<?> component = resolveRawClass(arrayType.getGenericComponentType(), subType);
      return Array.newInstance(component, 0).getClass();
    } else if (genericType instanceof TypeVariable) {
      TypeVariable<?> variable = (TypeVariable<?>) genericType;
      genericType = getTypeVariableMap(subType).get(variable);
      genericType = genericType == null ? resolveBound(variable)
              : resolveRawClass(genericType, subType);
    }

    return genericType instanceof Class ? (Class<?>) genericType : Unknown.class;
  }

  private static Map<TypeVariable<?>, Type> getTypeVariableMap(final Class<?> targetType) {
    Map<TypeVariable<?>, Type> map = new HashMap<TypeVariable<?>, Type>();

    // Populate interfaces
    populateSuperTypeArgs(targetType.getGenericInterfaces(), map, false);

    // Populate super classes and interfaces
    Type genericType = targetType.getGenericSuperclass();
    Class<?> type = targetType.getSuperclass();
    while (type != null && !Object.class.equals(type)) {
      if (genericType instanceof ParameterizedType)
        populateTypeArgs((ParameterizedType) genericType, map, false);
      populateSuperTypeArgs(type.getGenericInterfaces(), map, false);

      genericType = type.getGenericSuperclass();
      type = type.getSuperclass();
    }

    // Populate enclosing classes
    type = targetType;
    while (type.isMemberClass()) {
      genericType = type.getGenericSuperclass();
      if (genericType instanceof ParameterizedType)
        populateTypeArgs((ParameterizedType) genericType, map, false);

      type = type.getEnclosingClass();
    }

    return map;
  }

  /**
   * Populates the {@code map} with with variable/argument pairs for the given {@code types}.
   */
  private static void populateSuperTypeArgs(final Type[] types, final Map<TypeVariable<?>, Type> map, boolean depthFirst) {
    for (Type type : types) {
      if (type instanceof ParameterizedType) {
        ParameterizedType parameterizedType = (ParameterizedType) type;
        if (!depthFirst)
          populateTypeArgs(parameterizedType, map, depthFirst);
        Type rawType = parameterizedType.getRawType();
        if (rawType instanceof Class)
          populateSuperTypeArgs(((Class<?>) rawType).getGenericInterfaces(), map, depthFirst);
        if (depthFirst)
          populateTypeArgs(parameterizedType, map, depthFirst);
      } else if (type instanceof Class) {
        populateSuperTypeArgs(((Class<?>) type).getGenericInterfaces(), map, depthFirst);
      }
    }
  }

  /**
   * Populates the {@code map} with variable/argument pairs for the given {@code type}.
   */
  private static void populateTypeArgs(ParameterizedType type, Map<TypeVariable<?>, Type> map, boolean depthFirst) {
    if (type.getRawType() instanceof Class) {
      TypeVariable<?>[] typeVariables = ((Class<?>) type.getRawType()).getTypeParameters();
      Type[] typeArguments = type.getActualTypeArguments();

      if (type.getOwnerType() != null) {
        Type owner = type.getOwnerType();
        if (owner instanceof ParameterizedType)
          populateTypeArgs((ParameterizedType) owner, map, depthFirst);
      }

      for (int i = 0; i < typeArguments.length; i++) {
        TypeVariable<?> variable = typeVariables[i];
        Type typeArgument = typeArguments[i];

        if (typeArgument instanceof Class) {
          map.put(variable, typeArgument);
        } else if (typeArgument instanceof GenericArrayType) {
          map.put(variable, typeArgument);
        } else if (typeArgument instanceof ParameterizedType) {
          map.put(variable, typeArgument);
        } else if (typeArgument instanceof TypeVariable) {
          TypeVariable<?> typeVariableArgument = (TypeVariable<?>) typeArgument;
          if (depthFirst) {
            Type existingType = map.get(variable);
            if (existingType != null) {
              map.put(typeVariableArgument, existingType);
              continue;
            }
          }

          Type resolvedType = map.get(typeVariableArgument);
          if (resolvedType == null)
            resolvedType = resolveBound(typeVariableArgument);
          map.put(variable, resolvedType);
        }
      }
    }
  }

  /**
   * Resolves the first bound for the {@code typeVariable}, returning {@code Unknown.class} if none can be resolved.
   */
  private static Type resolveBound(TypeVariable<?> typeVariable) {
    Type[] bounds = typeVariable.getBounds();
    if (bounds.length == 0)
      return Unknown.class;

    Type bound = bounds[0];
    if (bound instanceof TypeVariable)
      bound = resolveBound((TypeVariable<?>) bound);

    return bound == Object.class ? Unknown.class : bound;
  }
}
