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

package org.apache.toy.common;

import org.apache.toy.annotation.Nullable;

import java.util.*;

/**
 * A class defines parameter. Use {@link Builder} to create a Parameter.
 * A parameter is composed of a key, a description, and a flag indicating if it is a must, and value of type.
 * @param <T>
 */
public final class Parameter<T> {

  private String key;
  private T value;
  private String description;
  private boolean required;
  private Class<?> type;
  private List<Constraint> constraints;

  private Parameter() {}

  private Parameter(String key, String description, boolean required, Class<?> type, T value, List<Constraint> constraints) {
    this.key = key;
    this.description = description;
    this.required = required;
    this.value = value;
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
   * @return true is value is empty, false otherwise
   */
  public boolean empty() {
    return value == null;
  }

  /**
   * Check value and set if value is not null.
   * @param value a nullable value
   */
  public void checkAndSet(@Nullable T value) {
    Optional<T> v = Optional.ofNullable(value);
    if (v.isPresent()) {
      for (Constraint constraint : constraints) {
        if (!constraint.satisfyContraint(value)) {
          throw new IllegalArgumentException(constraint.getConstraint(value));
        }
      }
      setValue(value);
    }
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
    private List<Constraint> constraints;

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
     * Add constraint for this parameter, for an interger value example, we want it to be larger than 10.
     * Then we can call addConstraint(Condition.GREATER, 10);
     * @param cond condition to be checked
     * @param expect expect value
     * @return builder itself
     */
    public Builder<T> addConstraint(Condition cond, Object expect) {
      constraints.add(new Constraint(cond, expect) {

        @Override boolean satisfyContraint(Object exact_value) {
          return condition.satisfyContraint(expect_value, exact_value);
        }

      });
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

  private static abstract class Constraint {

    Condition condition;
    Object expect_value;

    Constraint(Condition condition, Object expect_value) {
      this.condition = condition;
      this.expect_value = expect_value;
    }

    abstract boolean satisfyContraint(Object exact_value);

    String getConstraint(Object exact_value) {
      return "Need to satisify constrain: {" + condition.name + " " + expect_value + "}, but exact value is " + exact_value;
    }

  }

  /**
   * <, <=, >=, >, ==, in
   */
  public enum Condition {

    EQUAL("=") {

      @Override boolean satisfyContraint(Object expect_value, Object exact_value) {
        return expect_value == exact_value || expect_value.equals(exact_value);
      }
    },

    LESS("<") {

      @Override boolean satisfyContraint(Object expect_value, Object exact_value) {
        if (expect_value instanceof Integer) {
          return ((Integer) expect_value).compareTo((Integer) exact_value) > 0;
        } else if (expect_value instanceof Long) {
          return ((Long) expect_value).compareTo((Long) exact_value) > 0;
        } else {
          return ((Double) expect_value).compareTo((Double) exact_value) > 0;
        }
      }
    },

    LESS_EQUAL("<=") {

      @Override boolean satisfyContraint(Object expect_value, Object exact_value) {
        if (expect_value instanceof Integer) {
          return ((Integer) expect_value).compareTo((Integer) exact_value) >= 0;
        } else if (expect_value instanceof Long) {
          return ((Long) expect_value).compareTo((Long) exact_value) >= 0;
        } else {
          return ((Double) expect_value).compareTo((Double) exact_value) >= 0;
        }
      }

    },

    GREATER(">") {

      @Override boolean satisfyContraint(Object expect_value, Object exact_value) {
        if (expect_value instanceof Integer) {
          return ((Integer) expect_value).compareTo((Integer) exact_value) < 0;
        } else if (expect_value instanceof Long) {
          return ((Long) expect_value).compareTo((Long) exact_value) < 0;
        } else {
          return ((Double) expect_value).compareTo((Double) exact_value) < 0;
        }
      }

    },

    GREATER_EQUAL(">=") {

      @Override boolean satisfyContraint(Object expect_value, Object exact_value) {
        if (expect_value instanceof Integer) {
          return ((Integer) expect_value).compareTo((Integer) exact_value) <= 0;
        } else if (expect_value instanceof Long) {
          return ((Long) expect_value).compareTo((Long) exact_value) <= 0;
        } else {
          return ((Double) expect_value).compareTo((Double) exact_value) <= 0;
        }
      }

    };

    String name;

    Condition(String name) { this.name = name; }

    abstract boolean satisfyContraint(Object expect_value, Object exact_value);

  }

}
