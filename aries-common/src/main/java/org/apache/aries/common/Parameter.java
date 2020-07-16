/*
 * Copyright (c) 2019 R.C
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.aries.common;

import java.util.ArrayList;
import java.util.List;

/**
 * A class defines parameter. Use {@link Builder} to create a Parameter.
 * A parameter is composed of a key, a description, and a flag indicating if it is a must, and value of type.
 * In addition, user can add some constraints on parameter.
 * @param <T>
 */
public final class Parameter<T> {

  private String key;
  private T value;
  private String description;
  private boolean required;
  private Class<?> type;
  private List<ConstraintFunction<T>> constraints;
  private T default_value;

  private Parameter() {}

  private Parameter(String key, String description, boolean required, Class<?> type, T value, List<ConstraintFunction<T>> constraints) {
    this.key = key;
    this.description = description;
    this.required = required;
    this.value = value;
    this.default_value = value;
    this.type = type;
    this.constraints = constraints;
  }

  /**
   * Get key name of parameter.
   * @return key
   */
  public String key() {
    return key;
  }

  /**
   * Set value for parameter.
   * @param value value for this parameter
   */
  public void setValue(T value) {
    this.value = value;
  }

  /**
   * Get value of parameter.
   * @return value
   */
  public T value() {
    return value;
  }

  /**
   * Get default value of parameter.
   * @return default value
   */
  public T defvalue() {
    return default_value;
  }

  /**
   * Get type of value.
   * @return value's class
   */
  public Class<?> type() {
    return type;
  }

  /**
   * Description of parameter.
   * @return description
   */
  public String description() {
    return description;
  }

  /**
   * @return true if parameter is a must, false otherwise
   */
  public boolean required() {
    return required;
  }

  /**
   * @return true if it is null.
   */
  public boolean empty() {
    return value == null;
  }

  /**
   * Check value and set if value is neither null nor default value.
   * @param value a non-null value
   */
  public void checkAndSet(T value) {
         if (type().equals(String.class)   && value.equals(Constants.UNSET_STRING))    return;
    else if (type().equals(String[].class) && value == Constants.UNSET_STRINGS)        return;
    else if (type().equals(Integer.class)  && (Integer) value == Constants.UNSET_INT)  return;
    else if (type().equals(Long.class)     && (Long)value == Constants.UNSET_LONG)     return;
    else if (type().equals(Float.class)    && (Float)value == Constants.UNSET_FLOAT)   return;
    else if (type().equals(Double.class)   && (Double)value == Constants.UNSET_DOUBLE) return;
    else if (type().equals(Short.class)    && (Short)value == Constants.UNSET_SHORT)   return;
    // Ingore enum type and boolean type

    for (ConstraintFunction<T> constraint : constraints) {
      if (!constraint.satisfy(value)) {
        throw new IllegalArgumentException(key + "'s value " +  value + " doesn't satisfy one of the parameter constrains.");
      }
    }
    setValue(value);
  }

  /**
   * @return value in string
   */
  public String valueInString() {
    return empty() ? "NULL" : decorateString();
  }

  private String decorateString() {
    if (!type().equals(String[].class)) {
      return String.valueOf(value());
    }
    String[] vs = (String[]) value;
    StringBuilder builder = new StringBuilder();
    for (String v : vs) {
      builder.append(v).append(",");
    }
    String s = builder.toString();
    return s.substring(0, s.lastIndexOf(","));
  }

  /**
   * A builder for creating parameter.
   * @param <T> type
   * @return a builder
   */
  public static <T> Builder<T> newBuilder() {
    return new Builder<>();
  }

  public static class Builder<T> {

    private String key;
    private String description;
    private boolean required;
    private Class<?> type;
    private T value;
    private List<ConstraintFunction<T>> constraints;

    private Builder() {
      key = "";
      description = "";
      required = false;
      value = null;
      constraints = new ArrayList<>();
    }

    /**
     * Set key for paramter.
     * @param key key
     * @return builder itself
     */
    public Builder<T> setKey(String key) {
      this.key = key;
      return this;
    }

    /**
     * Set description for paramter.
     * @param description description
     * @return builder itself
     */
    public Builder<T> setDescription(String description) {
      this.description = description.toLowerCase();
      return this;
    }

    /**
     * Set if it is parameter is a must.
     * @param required true or false
     * @return builder itself
     */
    public Builder<T> setRequired(boolean required) {
      this.required = required;
      return this;
    }

    /**
     * Set type of value.
     * @param type type of value
     * @return buildser itself
     */
    public Builder<T> setType(Class<?> type) {
      this.type = type;
      return this;
    }

    /**
     * Set default value of this parameter
     * @param value default value
     * @return builder itself
     */
    public Builder<T> setDefaultValue(T value) {
      this.value = value;
      return this;
    }

    /**
     * Add constraint function for this parameter, for an interger value example, we want it to be larger than 10.
     * Then we can call addConstraint(l -> 10);
     * @param cond condition to be checked
     * @return builder itself
     */
    public Builder<T> addConstraint(ConstraintFunction<T> cond) {
      constraints.add(cond);
      return this;
    }

    /**
     * Create a parameter.
     * @return A parameter
     */
    public Parameter<T> opt() {
      return new Parameter<>(key, description, required, type, value, constraints);
    }

  }

}
