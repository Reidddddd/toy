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
import org.apache.toy.annotation.ThreadSafe;
import org.apache.toy.common.FileLineIterator;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Configuration file for Java application.
 */
@ThreadSafe
public final class Configuration {

  private static final String HEADER = "# Copyright (c) R.C";
  private static final String KV_DELIMITER = "=";

  private Map<String, String> configurations = new HashMap<>();

  private Configuration(Map<String, String> key_value_pairs) {
    key_value_pairs.forEach((k, v) -> configurations.put(k, v));
  }

  /**
   * Don't directly call this method. Use {@link ConfigurationFactory#createJavaConfiguration}
   * @param config_file configuration file
   * @return java configuration
   */
  public static Configuration createConfiguration(File config_file) {
    Map<String, String> key_value_pairs = new HashMap<>();

    try (FileLineIterator fli = new FileLineIterator(config_file)) {
      boolean first_line = true;
      while (fli.hasNext()) {
        // Check header of configuration file
        if (first_line) {
          if (!checkHeader(fli.next())) {
            throw new RuntimeException("Unauthorized toy-site.conf");
          }
          first_line = false;
          continue;
        }

        // Skip comments and empty line
        String kv = fli.next();
        if (kv.isEmpty() || kv.startsWith("#") || kv.startsWith("//") || !kv.contains("=")) continue;

        // Real key-value pair
        String[] pair = kv.split(KV_DELIMITER);
        key_value_pairs.put(pair[0], pair[1]);
      }
    }

    return new Configuration(key_value_pairs);
  }

  private static boolean checkHeader(String line) {
    return line.contains(HEADER);
  }

  public int getInt(String key) {
    return Integer.parseInt(configurations.get(key));
  }

  public String get(String key) {
    return configurations.get(key);
  }

  public String[] getStrings(String key) {
    Optional<String> value = Optional.ofNullable(get(key));
    return value.isPresent() ? value.get().split(",") : null;
  }

}
