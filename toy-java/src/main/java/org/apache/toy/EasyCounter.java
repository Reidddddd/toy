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

public class EasyCounter extends AbstractJavaToy {

  public EasyCounter() {}

  @Override
  public int howToPlay(PrintStream out) {
    return RETURN_CODE.HELP.code();
  }

  @Override
  public void preCheck(Configuration configuration) {
  }

  @Override
  public int haveFun() throws Exception {
    System.out.println("Having fun!!!");
    return 0;
  }

}
