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

import org.junit.Test;

public class TestToyParameters {

  @Test(expected = IllegalArgumentException.class)
  public void testNullArgument() {
    ToyParameters.parse(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissToyArgument() {
    ToyParameters.parse(new String[] { "--conf_dir", "/" });
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissConfDirArgument() {
    ToyParameters.parse(new String[] { "--toy", "WhateverToy" });
  }

  @Test(expected = ArrayIndexOutOfBoundsException.class)
  public void testIncompleteToyArgument() {
    ToyParameters.parse(new String[] { "--toy" });
  }

  @Test(expected = ArrayIndexOutOfBoundsException.class)
  public void testIncompleteConfDirArgument() {
    ToyParameters.parse(new String[] { "--conf_dir" });
  }

}
