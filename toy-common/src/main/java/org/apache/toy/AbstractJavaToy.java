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

import org.apache.toy.common.HelpPrinter;
import org.apache.toy.common.Parameter;

import java.io.PrintStream;

/**
 * Java toy's base implementation. Java configuration is inititlized in this class.
 */
public abstract class AbstractJavaToy extends AbstractToy<Configuration> {

  @Override
  public int howToPlay(PrintStream out) {
    HelpPrinter.printUsage(out, this.getClass(), parameters);
    return RETURN_CODE.HELP.code();
  }

  @Override
  protected void preCheck(Configuration configuration) {
    for (Parameter parameter : parameters) {
           if (parameter.type().equals(String.class)) parameter.checkAndSet(configuration.get(parameter.key()));
      else if (parameter.type().equals(Integer.class)) parameter.checkAndSet(configuration.getInt(parameter.key()));
      else if (parameter.type().equals(String[].class)) parameter.checkAndSet(configuration.getStrings(parameter.key()));

      if (parameter.required() && parameter.empty()) {
        howToPlay(System.out);
        throw new IllegalArgumentException(parameter.key() + " is not set");
      }
    }
  }

  @Override
  public final int play(String dir_of_conf_file) throws Exception {
    Configuration configuration = ConfigurationFactory.createJavaConfiguration(dir_of_conf_file);
    preCheck(configuration);
    return haveFun();
  }

}
