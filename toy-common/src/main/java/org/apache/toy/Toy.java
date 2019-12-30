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

/**
 * Definition of a toy.
 */
public interface Toy {

  /**
   * Initialzation before playing toy, please add all user-defined parameters in this method.
   */
  void init();

  /**
   * How to play toy.
   * @param out stream for printing
   */
  int howToPlay(PrintStream out);

  /**
   * Because it is a toy, as it is name, exceptions are handled casually.
   * @param dir_of_conf_file
   * @return a code to denote success or failure
   * @throws Exception
   */
  int play(String dir_of_conf_file) throws Exception;
}
