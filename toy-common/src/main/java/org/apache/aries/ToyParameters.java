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

import org.apache.aries.annotation.Nullable;
import org.apache.aries.common.Constants;
import org.apache.aries.common.HelpPrinter;
import org.apache.aries.common.Parameter;
import org.apache.aries.common.StringParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ToyParameters {

  private static final Parameter<String> help =
      StringParameter.newBuilder("--help").setDescription("help message of toy").opt();
  private static final Parameter<String> toy =
      StringParameter.newBuilder("--toy").setDescription("toy to be run").setRequired().opt();
  private static final Parameter<String> conf =
      StringParameter.newBuilder("--conf_dir").setDescription("directory of configuration").opt();

  private static final List<Parameter> parameters = new ArrayList<>();

  static {
    parameters.add(help);
    parameters.add(toy);
    parameters.add(conf);
  }

  private boolean need_help;
  private String toy_name;
  private String conf_dir;

  private ToyParameters(String toy_name, String conf_dir) {
    this(Constants.UNSET_FALSE, toy_name, conf_dir);
  }

  private ToyParameters() {
    this(true, Constants.UNSET_STRING, Constants.UNSET_STRING);
  }

  private ToyParameters(boolean need_help, String toy_name, String conf_dir) {
    this.need_help = need_help;
    this.toy_name = toy_name;
    this.conf_dir =  conf_dir;
  }

  public boolean needHelp() {
    return need_help;
  }

  public String getToyName() {
    return toy_name;
  }

  public String getConfDirectory() {
    return conf_dir;
  }

  public static ToyParameters parse(@Nullable String[] main_args) {
    Optional<String[]> optional_args = Optional.ofNullable(main_args);
    if (!optional_args.isPresent()) {
      throw new IllegalArgumentException("Found no argument");
    }

    String toy_name = Constants.UNSET_STRING;
    String conf_dir = Constants.UNSET_STRING;
    boolean need_help = false;
    String[] args = optional_args.get();
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals(help.key())) need_help = true;

      if (args[i].equals(toy.key())) {
        if (++i == args.length) throw new ArrayIndexOutOfBoundsException("Value for " + toy.key() + " is not set.");
       toy_name = args[i];
      } else if (args[i].equals(conf.key())) {
        if (++i == args.length) throw new ArrayIndexOutOfBoundsException("Value for " + conf.key() + " is not set.");
        conf_dir = args[i];
      }
    }

    if (toy_name.equals(Constants.UNSET_STRING)) {
      HelpPrinter.printUsage(System.out, ToyParameters.class, parameters);
      throw new IllegalArgumentException("Toy isn't specified");
    }
    if (!need_help && conf_dir.equals(Constants.UNSET_STRING)) {
      HelpPrinter.printUsage(System.out, ToyParameters.class, parameters);
      throw new IllegalArgumentException("Either " + help.key() + " or " + conf.key() + " should be set");
    }
    return new ToyParameters(need_help, toy_name, conf_dir);
  }

}
