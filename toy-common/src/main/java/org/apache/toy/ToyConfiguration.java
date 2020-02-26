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

import org.apache.toy.annotation.ThreadSafe;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

@ThreadSafe
public class ToyConfiguration {

  private final Properties properties = new Properties();

  public ToyConfiguration(Path toy_property_file) throws IOException {
    properties.load(new FileReader(toy_property_file.toFile()));
  }

  public boolean containsKey(String key) {
    return properties.containsKey(key);
  }

  public String get(String key) {
    return containsKey(key) ? properties.getProperty(key) : Constants.UNSET_STRING;
  }

  public String[] getStrings(String key, String split_char) {
    String values = get(key);
    return values.isEmpty() ? Constants.UNSET_STRINGS : values.split(split_char);
  }

  public int getInt(String key) {
    String int_value = get(key);
    return int_value.isEmpty() ? Constants.UNSET_INT: Integer.parseInt(int_value);
  }

  public long getLong(String key) {
    String long_value = get(key);
    return long_value.isEmpty() ? Constants.UNSET_LONG: Long.parseLong(long_value);
  }

  public double getDouble(String key) {
    String double_value = get(key);
    return double_value.isEmpty() ? Constants.UNSET_DOUBLE : Double.parseDouble(double_value);
  }

  public float getFloat(String key) {
    String float_value = get(key);
    return float_value.isEmpty() ? Constants.UNSET_FLOAT : Float.parseFloat(float_value);
  }

  public boolean getBoolean(String key, boolean def_value) {
    String bool_value = get(key);
    return bool_value.isEmpty() ? def_value : Boolean.parseBoolean(bool_value);
  }

  public <T extends Enum<T>> T getEnum(String key, T def_value) {
    String enum_value = get(key);
    return enum_value.isEmpty() ? def_value : Enum.valueOf(def_value.getDeclaringClass(), enum_value);
  }

}
