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

import java.io.PrintStream;

public final class ProcessChecker extends AbstractJavaToy {

  private final Parameter<String> check_file = Parameter.newBuilder().setKey("check_file")
                                                        .setRequired(true).setDescription("File to checked")
                                                        .opt();
  private final Parameter<String> section_delimiter = Parameter.newBuilder().setKey("section_delimiter")
                                                               .setRequired(true).setDescription("Section separator")
                                                               .opt();

  @Override
  public int howToPlay(PrintStream out) {
    return RETURN_CODE.HELP.code();
  }

  @Override
  public void preCheck(Configuration configuration) {
    check_file.checkAndSet(configuration.get(check_file.key()));
    if (check_file.required() && check_file.empty()) {
      howToPlay(System.out);
      throw new IllegalArgumentException(check_file.key() + " is not set");
    }

    section_delimiter.checkAndSet(configuration.get(section_delimiter.key()));
    if (section_delimiter.required() && section_delimiter.empty()) {
      howToPlay(System.out);
      throw new IllegalArgumentException(section_delimiter.key() + " is not set");
    }
  }

  @Override
  public int haveFun() throws Exception {
    String file = check_file.value();
    try (FileLineIterator fli = new FileLineIterator(file)) {
      while (fli.hasNext()) {

      }
    }
    return 0;
  }

}
