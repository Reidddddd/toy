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

import org.apache.toy.common.HelpPrinter;
import org.apache.toy.common.Parameter;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * An abstract toy.
 * @param <T> configuration type, based on what kind of toy
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractToy<T> implements Toy {

  private final List<Parameter> parameters = new ArrayList<>();

  @Override
  public final void init() {
    requisite(parameters);
  }

  @Override
  public final int howToPlay(PrintStream out) {
    HelpPrinter.printUsage(out, this.getClass(), parameters);
    return RETURN_CODE.HELP.code();
  }

  @Override
  public final int play(String dir_of_conf_file) throws Exception {
    return play(dir_of_conf_file, parameters);
  }

  /**
   * Please add all user-defined parameters in this method.
   * @param requisites parameters to be added to
   */
  protected abstract void requisite(List<Parameter> requisites);

  /**
   * This method is used for checking parameters needed for playing toy. Toy should throw IllegalArgumentException
   * if any required parameter is not set.
   * @param configuration configuration file for reading parameter
   * @param requisites requisites to be checked
   */
  protected abstract void preCheck(T configuration, List<Parameter> requisites);

  /**
   * Build up toys.
   * @param configuration configurations
   * @throws Exception exception if any
   */
  protected abstract void buildToy(T configuration) throws Exception;

  /**
   * This method is used for playing toy.
   * Feel free to throw any exception, try not to deal within method body in order to make your toy light.
   * @param dir_of_conf_file directory of configuration
   * @param requisites parameters
   */
  protected abstract int play(String dir_of_conf_file, List<Parameter> requisites) throws Exception;

  /**
   * Actual toy playing.
   * @return {@link RETURN_CODE#SUCCESS} for good play, {@link RETURN_CODE#FAILURE} for bad play
   * @throws Exception throw exception when playing toy if any
   */
  protected abstract int haveFun() throws Exception;

  /**
   * After toy played, close resources if any.
   * @throws Exception throw exception if any
   */
  protected abstract void destroyToy() throws Exception;

  protected enum RETURN_CODE {
    HELP(-2),
    SUCCESS(0),
    FAILURE(1);

    private int return_code;

    RETURN_CODE(int return_code) {
      this.return_code = return_code;
    }

    protected int code() {
      return return_code;
    }
  }

}
