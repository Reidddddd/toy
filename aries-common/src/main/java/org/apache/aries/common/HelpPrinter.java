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

package org.apache.aries.common;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Tool class for print help message.
 */
public final class HelpPrinter {

  private static final Logger LOG = Logger.getLogger(HelpPrinter.class.getName());

  private HelpPrinter() {}

  private static void printParameters(@SuppressWarnings("rawtypes") List<Parameter> parameters, int indent) {
    parameters.forEach(p -> printParameter(p, indent));
  }

  private static String indent(int x) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < x; i++) builder.append(" ");
    return builder.toString();
  }
  private static void printParameter(Parameter<?> parameter, int indent) {
    LOG.info("| " + parameter.required() +
                  " | " + parameter.key() + indent(indent - parameter.key().length()) +
                  " | " + (parameter.defvalue() == null ? "NOT SET" : parameter.defvalue()) +
                  " | " + parameter.description() + " |");
  }

  @SuppressWarnings("rawtypes")
  public static void printUsage(Class<?> clazz, List<Parameter> args) {
    List<Parameter> required = new ArrayList<>();
    List<Parameter> optional = new ArrayList<>();
    args.forEach(arg -> {
      if (arg.required()) required.add(arg);
      else optional.add(arg);
    });
    required.sort((o1, o2) -> o2.key().length() - o1.key().length());
    optional.sort((o1, o2) -> o2.key().length() - o1.key().length());

    String clazz_name = clazz.getSimpleName();
    LOG.info("Toy: " + clazz_name + " has following parameters:");
    LOG.info("|------ Required ------|------ Key -------|------ Default ------|------ Description ------|");
    if (!required.isEmpty()) printParameters(required, required.get(0).key().length());
    if (!optional.isEmpty()) printParameters(optional, optional.get(0).key().length());
  }

}
