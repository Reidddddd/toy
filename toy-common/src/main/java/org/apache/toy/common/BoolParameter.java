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

public final class BoolParameter {

  /**
   * A builder for creating boolean parameter.
   * @param key key name for this parameter
   * @param value default value
   * @return a boolean parameter builder
   */
  public static BoolBuilder newBuilder(String key, boolean value) {
    return new BoolBuilder(key, value);
  }

  private BoolParameter() {}

  public static class BoolBuilder extends AbstractTypeBuilder<Boolean> {

    private BoolBuilder(String key, boolean value) {
      builder.setKey(key).setType(Boolean.class).setDefaultValue(value);
    }

  }

}
