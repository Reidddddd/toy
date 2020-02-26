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

public abstract class AbstractTypeBuilder<T> {

  protected Parameter.Builder<T> builder = Parameter.newBuilder();

  /**
   * Set description for paramter.
   * @param description description
   * @return builder itself
   */
  public AbstractTypeBuilder<T> setDescription(String description) {
    builder.setDescription(description.toLowerCase());
    return this;
  }

  /**
   * Set if it is parameter is a must, by default is false.
   * @return builder itself
   */
  public AbstractTypeBuilder<T> setRequired() {
    builder.setRequired(true);
    return this;
  }

  /**
   * Add constraint function for this parameter, for an interger value example, we want it to be larger than 10.
   * Then we can call addConstraint(l -> 10);
   * @param cond condition to be checked
   * @return builder itself
   */
  public AbstractTypeBuilder<T> addConstraint(ConstraintFunction<T> cond) {
    builder.addConstraint(cond);
    return this;
  }

  /**
   * Create a parameter.
   * @return A parameter
   */
  public Parameter<T> opt() {
    return builder.opt();
  }

}
