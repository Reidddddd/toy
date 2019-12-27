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

import java.util.Optional;

public final class ToyParameters {

  private boolean need_help;
  private String class_name;
  private String conf_dir;

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

    String class_name = "";
    String conf_dir = "";
    boolean help = false;
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("--class") || args[i].equals("-c")) {
        class_name = args[++i];
        continue;
      }
      if (args[i].equals("--help") || args[i].equals("-h")) {
        help = true;
        continue;
      }
      if (args[i].equals("--conf_dir") || args[i].equals("-cd")) {
        conf_dir = args[++i];
      }
    }

    if (class_name.equals("")) {
      throw new IllegalArgumentException("Class name must be specified");
    }
    if (conf_dir.equals("")) {
      throw new IllegalArgumentException("Configuration directory must be specified");
    }
    return new ToyParameters(help, class_name, conf_dir);
  }
}
