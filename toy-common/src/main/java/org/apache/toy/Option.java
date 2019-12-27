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

public final class Option<T> {

  private String full_key;
  private String brief_key;
  private T value;
  private String description;
  private boolean required;

  private Option(String full_key, String brief_key, String description, boolean required) {
    this.full_key = full_key;
    this.brief_key = brief_key;
    this.description = description;
    this.required = required;
  }

  public void setValueName(T value) {
    this.value = value;
  }

  public String getFullKeyName() {
    return full_key;
  }

  public String getBriefKeyName() {
    return brief_key;
  }

  public T getValue() {
    return value;
  }

  public boolean isRequired() {
    return required;
  }

  public boolean isEmpty() {
    return value == null;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {

    private String full_key;
    private String brief_key;
    private String description;
    private boolean required;

    private Builder() {
      full_key = "";
      brief_key = "";
      description = "";
      required = false;
    }

    public Builder setFullKeyName(String full_key) {
      this.full_key = full_key;
      return this;
    }

    public Builder setBriefKeyName(String brief_key) {
      this.brief_key = brief_key;
      return this;
    }

    public Builder setDescription(String description) {
      this.description = description;
      return this;
    }

    public Builder setRequired(boolean required) {
      this.required = required;
      return this;
    }

    public <T> Option<T> opt() {
      return new Option<>(full_key, brief_key, description, required);
    }

  }

}
