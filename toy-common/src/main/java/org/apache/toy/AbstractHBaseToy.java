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

import org.apache.hadoop.conf.Configuration;
import org.apache.toy.common.Parameter;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.toy.common.Parameter.UNSET;

/**
 * HBase toy's base implementation. HBase configuration is inititlized in this class.
 */
public abstract class AbstractHBaseToy extends AbstractToy<Configuration> {

  private Set<String> keys_set = new HashSet<>();

  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  protected final void preCheck(Configuration configuration, List<Parameter> requisites) {
    for (Parameter p : requisites) {
      if (!keys_set.contains(p.key())) {
        if (p.required()) {
          howToPlay(System.out);
          throw new IllegalArgumentException(p.key() + " is not set");
        }
        continue;
      }
           if (p.type().equals(String.class))   p.checkAndSet(configuration.get(p.key()));
      else if (p.type().equals(String[].class)) p.checkAndSet(configuration.getStrings(p.key()));
      else if (p.type().isEnum())               p.checkAndSet(configuration.getEnum(p.key(), (Enum)p.value()));
      else if (p.type().equals(Integer.class))  p.checkAndSet(configuration.getInt(p.key(), (Integer)UNSET));
      else if (p.type().equals(Boolean.class))  p.checkAndSet(configuration.getBoolean(p.key(), (Boolean)p.value()));
    }
  }

  @Override
  protected final int play(String dir_of_conf_file,
                           @SuppressWarnings("rawtypes") List<Parameter> requisites) throws Exception {
    Configuration configuration = ConfigurationFactory.createHBaseConfiguration(dir_of_conf_file);
    cacheConfigurations(configuration.iterator());
    preCheck(configuration, requisites);
    buildToy(configuration);
    try {
      return haveFun();
    } finally {
      destroyToy();
    }
  }

  private void cacheConfigurations(Iterator<Map.Entry<String, String>> iterator) {
    while (iterator.hasNext()) {
      keys_set.add(iterator.next().getKey());
    }
  }

}
