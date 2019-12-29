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

package org.apache.toy;

import org.apache.toy.annotation.Nullable;

import java.util.Optional;

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

  private Parameter() {}

  private Parameter(String key, String description, boolean required) {
    this.key = key;
    this.description = description;
    this.required = required;
    this.value = null;
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
   * @param value
   */
  public void setValue(T value) {
    this.value = value;
  }

  /**
   * Get value of parameter.
   * @return
   */
  public T value() {
    return value;
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
   * @return true it is not null and set, false otherwise
   */
  public boolean checkAndSet(T value) {
    @Nullable Optional<T> v = Optional.ofNullable(value);
    if (v.isPresent()) {
      setValue(value);
      return true;
    }
    return false;
  }

  /**
   * A builder for creating parameter.
   * @return A builder
   */
  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {

    private String key;
    private String description;
    private boolean required;

    private Builder() {
      key = "";
      description = "";
      required = false;
    }

    /**
     * Set key for paramter.
     * @param key key
     * @return Builder itself
     */
    public Builder setKey(String key) {
      this.key = key;
      return this;
    }

    /**
     * Set description for paramter.
     * @param description description
     * @return Builder itself
     */
    public Builder setDescription(String description) {
      this.description = description;
      return this;
    }

    /**
     * Set if it is parameter is a must.
     * @param required true or false
     * @return Builder itself
     */
    public Builder setRequired(boolean required) {
      this.required = required;
      return this;
    }

    /**
     * Create a parameter.
     * @param <T> type of value
     * @return A parameter
     */
    public <T> Parameter<T> opt() {
      return new Parameter<>(key, description, required);
    }

  }

}
