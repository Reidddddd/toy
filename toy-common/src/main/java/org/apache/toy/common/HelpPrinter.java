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

package org.apache.toy.common;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Tool class for print help message.
 */
public final class HelpPrinter {

  private HelpPrinter() {}

  private static void printIndents(PrintStream out) {
    // 6 spaces
    out.print("\\s\\s\\s\\s\\s\\s");
  }

  private static void printParameters(PrintStream out,
                                      @SuppressWarnings("rawtypes") List<Parameter> parameters, boolean required) {
    printIndents(out);
    out.println(required ? "Required:" : "Optional:");
    parameters.forEach(p -> {
      printIndents(out);
      printIndents(out);
      printParameter(out, p);
    });
  }

  private static void printParameter(PrintStream out, Parameter<?> parameter) {
    out.println("Parameter key: " + parameter.key() + ", description: " + parameter.description() + ", required: " + parameter.required());
  }

  @SuppressWarnings("rawtypes")
  public static void printUsage(PrintStream out, Class<?> clazz, List<Parameter> args) {
    List<Parameter> required = new ArrayList<>();
    List<Parameter> optional = new ArrayList<>();
    args.forEach(arg -> {
      if (arg.required()) required.add(arg);
      else optional.add(arg);
    });

    String clazz_name = clazz.getCanonicalName();
    out.println("Toy: " + clazz_name + " has following parameters.");
    printParameters(out, required, true);
    printParameters(out, optional, false);
  }

}
