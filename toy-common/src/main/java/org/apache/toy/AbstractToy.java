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
 */
public abstract class AbstractToy implements Toy {

  @SuppressWarnings("rawtypes")
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
    ToyConfiguration toy_conf = ToyConfiguration.create(dir_of_conf_file);
    preCheck(toy_conf, parameters);
    inCheck();
    buildToy(toy_conf);
    try {
      return haveFun();
    } finally {
      destroyToy();
    }
  }

  /**
   * Please add all user-defined parameters in this method.
   * @param requisites parameters to be added to
   */
  protected abstract void requisite(@SuppressWarnings("rawtypes") List<Parameter> requisites);

  /**
   * This method is used for checking parameters needed for playing toy. Toy should throw IllegalArgumentException
   * if any required parameter is not set.
   * @param configuration toy configuration
   * @param requisites requisites to be checked
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  private void preCheck(ToyConfiguration configuration, List<Parameter> requisites) {
    for (Parameter p : requisites) {
      if (!configuration.containsKey(p.key())) {
        if (p.required()) {
          howToPlay(System.out);
          throw new IllegalArgumentException(p.key() + " is not set");
        }
        continue;
      }
           if (p.type().equals(String.class))   p.checkAndSet(configuration.get(p.key()));
      else if (p.type().equals(String[].class)) p.checkAndSet(configuration.getStrings(p.key(), ","));
      else if (p.type().isEnum())               p.checkAndSet(configuration.getEnum(p.key(), (Enum)p.value()));
      else if (p.type().equals(Integer.class))  p.checkAndSet(configuration.getInt(p.key()));
      else if (p.type().equals(Boolean.class))  p.checkAndSet(configuration.getBoolean(p.key(), (Boolean)p.value()));
      else if (p.type().equals(Short.class))    p.checkAndSet(configuration.getShort(p.key()));
    }
  }

  /**
   * This method is called after preCheck, it may be used in specific toy to do self parameter check.
   * Every parameter should have valid value.
   */
  protected void inCheck() {
  }

  /**
   * Build up toys.
   * @param configuration toy configuration
   * @throws Exception exception if any
   */
  protected abstract void buildToy(ToyConfiguration configuration) throws Exception;

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
