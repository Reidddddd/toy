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

package org.apache.aries;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.aries.annotation.ThreadSafe;

import java.io.FileNotFoundException;
import java.nio.file.NotDirectoryException;
import java.util.Map;

/**
 * Factory for creating various kind of configuration. So far support HBase and Java.
 */
@ThreadSafe
public final class ConfigurationFactory {

  private ConfigurationFactory() {}

  /**
   * Create hbase configuration.
   * @param toy_conf toy configuration
   * @return wrapped hbase configuration
   * @throws NotDirectoryException if passes in parameter is not a directory
   * @throws FileNotFoundException if dir doesn't contain hbase-site.xml
   */
  public static synchronized final Configuration createHBaseConfiguration(ToyConfiguration toy_conf) {
    Configuration configuration = HBaseConfiguration.create();
    for (Map.Entry<Object, Object> e : toy_conf.getProperties().entrySet()) {
      configuration.set((String)e.getKey(), (String)e.getValue());
    }
    return configuration;
  }

}
