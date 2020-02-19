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

import org.apache.toy.common.Parameter;

import java.util.List;

/**
 * Java toy's base implementation. Java configuration is inititlized in this class.
 */
public abstract class AbstractJavaToy extends AbstractToy<Configuration> {

  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  protected final void preCheck(Configuration configuration, List<Parameter> requisites) {
    for (Parameter p : requisites) {
      if (!configuration.checkKey(p.key())) {
        if (p.required()) {
          howToPlay(System.out);
          throw new IllegalArgumentException(p.key() + " is not set");
        }
        continue;
      }
           if (p.type().equals(String.class))   p.checkAndSet(configuration.get(p.key()));
      else if (p.type().equals(String[].class)) p.checkAndSet(configuration.getStrings(p.key()));
      else if (p.type().isEnum())               p.checkAndSet(configuration.getEnum(p.key(), (Enum)p.value()));
      else if (p.type().equals(Integer.class))  p.checkAndSet(configuration.getInt(p.key()));
      else if (p.type().equals(Boolean.class))  p.checkAndSet(configuration.getBoolean(p.key(), (Boolean)p.value()));
    }
  }

  @Override
  public final int play(String dir_of_conf_file,
                        @SuppressWarnings("rawtypes") List<Parameter> requisites) throws Exception {
    Configuration configuration = ConfigurationFactory.createJavaConfiguration(dir_of_conf_file);
    preCheck(configuration, requisites);
    buildToy(configuration);
    try {
      return haveFun();
    } finally {
      destroyToy();
    }
  }

}
