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

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Usage of this class:
 * java -cp jarA:jarB --toy|-t full.class.name --conf_dir|-cd conf_dir --help|-h
 * Other runtime parameters please set them in respective configuration file, for simplicity.
 */
public final class ToyPlayer {

  private static Logger LOG = Logger.getLogger(ToyPlayer.class.getName());

  public static void main(String[] args) throws Exception {
    ToyParameters tp = ToyParameters.parse(args);
    buildLog(tp.getConfDirectory());
    LOG.info("Building Toy: " + tp.getToyName());
    Toy toy = (Toy) Class.forName(tp.getToyName()).newInstance();
    LOG.info("Initializing Toy: " + tp.getToyName());
    toy.init();
    LOG.info("Start to play Toy: " + tp.getToyName());
    System.exit(tp.needHelp() ? toy.howToPlay(System.out) : toy.play(tp.getConfDirectory()));
  }

  private static void buildLog(String directory) throws IOException {
    Path log_properties = Paths.get(directory, "logging.properties");
    LogManager.getLogManager().readConfiguration(new FileInputStream(log_properties.toFile()));
  }

}
