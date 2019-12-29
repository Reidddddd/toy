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
 * An abstract toy.
 * @param <T> configuration type, based on what kind of toy
 */
public abstract class AbstractToy<T> implements Toy {

  /**
   * This method is used for checking parameters needed for playing toy. Toy should throw IllegalArgumentException
   * if any required parameter is not set.
   * @param configuration configuration file for reading parameter
   * @throws Exception if parameters is not enough or wrong configured
   */
  public abstract void preCheck(T configuration);

  /**
   * Playing toy.
   * @return {@link RETURN_CODE#SUCCESS} for good play, {@link RETURN_CODE#FAILURE} for bad play
   * @throws Exception throw exception when playing toy if any
   */
  public abstract int haveFun() throws Exception;

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
