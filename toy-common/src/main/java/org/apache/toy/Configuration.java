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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/**
 * Configuration file for Java application.
 */
@ThreadSafe
public class Configuration {
  private static final String HEADER = "# Copyright (c) R.C";
  private static final String DELIMITER = "=";

  private Map<String, String> configurations = new HashMap<>();

  private Configuration(Map<String, String> key_value_pairs) {
    key_value_pairs.forEach((k, v) -> configurations.put(k, v));
  }

  /**
   * Don't directly call this method. Use {@link ConfigurationFactory#createJavaConfiguration}
   * @param config_file
   * @return java configuration
   */
  public static Configuration createConfiguration(File config_file) {
    System.out.println(config_file);
    Map<String, String> key_value_pairs = new HashMap<>();
    try (BufferedReader r = new BufferedReader(new FileReader(config_file))) {
      @Nullable Optional<String> line = Optional.ofNullable(r.readLine());
      boolean first_line = true;
      for (;line.isPresent(); line = Optional.ofNullable(r.readLine())) {
        // Check header of configuration file
        if (first_line) {
          if (!checkHeader(line.get())) {
            throw new RuntimeException("Unauthorized toy-site.conf");
          }
          first_line = false;
          continue;
        }

        // Skip comments and empty line.
        String kv = line.get();
        if (kv.isEmpty() || kv.startsWith("#") || kv.startsWith("//") || !kv.contains("=")) continue;

        // Real key-value pair
        String[] pair = kv.split(DELIMITER);
        key_value_pairs.put(pair[0], pair[1]);
      }
    } catch (FileNotFoundException e) {
      // FileNotFound should be guarded above, here can't be reached.
      // Just ignore.
    } catch (IOException e) {
      // what else?
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

  public static void main(String[] args) {
    Configuration conf = ConfigurationFactory.createJavaConfiguration("/Users/reid.chen/Desktop");
    System.out.println(conf.get("who"));
    System.out.println(conf.get("job"));
    System.out.println(conf.getInt("a"));
    System.out.println(conf.getInt("b"));
    System.out.println(conf.getInt("c"));
    System.out.println(conf.getInt("d"));
  }
}
