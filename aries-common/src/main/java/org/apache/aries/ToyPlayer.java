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

/**
 * Usage of this class:
 * java -cp jarA:jarB --toy|-t full.class.name --conf_dir|-cd conf_dir --help|-h
 * Other runtime parameters please set them in respective configuration file, for simplicity.
 */
public final class ToyPlayer {

  public static void main(String[] args) throws Exception {
    ToyParameters tp = ToyParameters.parse(args);
    Toy toy = (Toy) Class.forName(tp.getToyName()).newInstance();
    toy.init();
    System.exit(tp.needHelp() ? toy.howToPlay(System.out) : toy.play(tp.getConfDirectory()));
  }

}
