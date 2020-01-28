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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ToyParameters {

  private static final Parameter<Boolean> help =
      Parameter.<Boolean>newBuilder().setKey("--help").setDescription("help message of toy")
                                     .setType(Boolean.class)
                                     .opt();
  private static final Parameter<String> clazz =
      Parameter.<String>newBuilder().setKey("--class").setType(String.class)
                                    .setDescription("class to be run").setRequired(true)
                                    .opt();
  private static final Parameter<String> conf =
      Parameter.<String>newBuilder().setKey("--conf_dir").setDescription("directory of configuration")
                                    .setRequired(true).setType(String.class)
                                    .opt();
  private static final List<Parameter> parameters = new ArrayList<>();

  static {
    parameters.add(help);
    parameters.add(clazz);
    parameters.add(conf);
  }

  private boolean need_help;
  private String class_name;
  private String conf_dir;

  private ToyParameters() {}

  private ToyParameters(boolean need_help, String class_name, String conf_dir) {
    this.need_help = need_help;
    this.class_name = class_name;
    this.conf_dir =  conf_dir;
  }

  public boolean needHelp() {
    return need_help;
  }

  public String getClassName() {
    return class_name;
  }

  public String getConfDirectory() {
    return conf_dir;
  }

  public static ToyParameters parse(Optional<String[]> optional_args) {
    if (!optional_args.isPresent()) {
      throw new IllegalArgumentException("Found no argument");
    }

    String[] args = optional_args.get();
    help.setValue(false);

    for (int i = 0; i < args.length; i++) {
      if (args[i].equals(clazz.key())) {
        if (++i == args.length) {
          throw new ArrayIndexOutOfBoundsException("Value for " + clazz.key() + " is not set.");
        }
        clazz.setValue(args[i]);
        continue;
      }
      if (args[i].equals(conf.key())) {
        if (++i == args.length) {
          throw new ArrayIndexOutOfBoundsException("Value for " + conf.key() + " is not set.");
        }
        conf.setValue(args[i]);
        continue;
      }
      if (args[i].equals(help.key())) {
        help.setValue(true);
      }
    }

    for (Parameter parameter : parameters) {
      if (parameter.required() && parameter.empty()) {
        HelpPrinter.printUsage(System.out, ToyParameters.class, parameters);
        throw new IllegalArgumentException(parameter.key() + " is not set");
      }
    }

    return new ToyParameters(help.value(), clazz.value(), conf.value());
  }

}
