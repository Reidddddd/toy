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

/**
 * Usage of this class:
 * java -cp jarA:jarB full.class.name.
 * Parameters only support (--help|-h) and (--debug|-d)
 * Other runtime parameters please set them in respective configuration file, for simplicity.
 */
public final class ToyPlayer {

  public static void main(String[] args) throws Exception {
    if (args.length < 1) {
      throw new IllegalArgumentException();
    }

    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("--class")) {
        String clazz_name = args[++i];
        System.out.println(clazz_name);
        Toy toy = (Toy) Class.forName(clazz_name).newInstance();
        toy.play(args[1]);
      }
    }
  }

}
