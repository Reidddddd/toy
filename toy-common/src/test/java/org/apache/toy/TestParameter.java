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

import org.apache.toy.common.Parameter;
import org.junit.Assert;
import org.junit.Test;

public class TestParameter {

  @Test public void testStringValue() {
    Parameter<String> sp = Parameter.<String>newBuilder().setKey("k").setRequired(true).setType(String.class).setDefaultValue("v").opt();
    Assert.assertTrue(sp.type().equals(String.class));
    Assert.assertTrue(sp.unset());
    Assert.assertTrue(sp.required());
    Assert.assertTrue(sp.value().equals("v"));
  }

  @Test public void testIntValue() {
    Parameter<Integer> ip = Parameter.<Integer>newBuilder().setKey("i").setType(Integer.class).setDefaultValue(Integer.MAX_VALUE).opt();
    Assert.assertTrue(ip.type().equals(Integer.class));
    Assert.assertTrue(ip.unset());
    Assert.assertFalse(ip.required());
    Assert.assertTrue(Integer.MAX_VALUE == ip.value());
  }

  @Test public void testEnumValue() {
    Parameter<Enum> ep = Parameter.<Enum>newBuilder().setKey("e").setType(TestConfiguration.TEST.class).setDefaultValue(TestConfiguration.TEST.DEFAULT).opt();
    Assert.assertTrue(ep.type().equals(TestConfiguration.TEST.class));
    Assert.assertTrue(ep.unset());
    Assert.assertFalse(ep.required());
    Assert.assertTrue(ep.value().equals(TestConfiguration.TEST.DEFAULT));
  }

  @Test public void testStringsValue() {
    Parameter<String[]> sap = Parameter.<String[]>newBuilder().setKey("sa").setType(String[].class).setDefaultValue(new String[] {"x", "y", "z"}).opt();
    Assert.assertTrue(sap.type().equals(String[].class));
    Assert.assertTrue(sap.unset());
    Assert.assertFalse(sap.required());
    Assert.assertTrue(3 == sap.value().length);
    Assert.assertTrue(sap.value()[0].equals("x"));
    Assert.assertTrue(sap.value()[1].equals("y"));
    Assert.assertTrue(sap.value()[2].equals("z"));
  }

  @Test public void testBooleanValue() {
    Parameter<Boolean> bp = Parameter.<Boolean>newBuilder().setKey("b").setType(Boolean.class).setDefaultValue(Boolean.FALSE).opt();
    Assert.assertTrue(bp.type().equals(Boolean.class));
    Assert.assertTrue(bp.unset());
    Assert.assertFalse(bp.required());
    Assert.assertFalse(bp.value());
  }

  @Test public void testEmpty() {
    Parameter<String> empty = Parameter.<String>newBuilder().setKey("empty").setType(String.class).opt();
    Assert.assertTrue(empty.empty());
  }

  @Test public void testSet() {
    Parameter<Integer> set = Parameter.<Integer>newBuilder().setKey("set").setType(Integer.class).opt();
    Assert.assertTrue(set.empty());
    set.setValue(Integer.MIN_VALUE);
    Assert.assertFalse(set.empty());
    Assert.assertFalse(set.unset());
    Assert.assertTrue(Integer.MIN_VALUE == set.value());
  }

  @Test public void testCheck() {
    Parameter<Integer> check =
        Parameter.<Integer>newBuilder()
            .setKey("check").setType(Integer.class).addConstraint(v -> v > 1).addConstraint(v -> v <= 10).opt();
    // Check null
    Assert.assertTrue(check.empty());
    check.checkAndSet(null);
    Assert.assertTrue(check.empty());
    // Check value doesn't satisfy constrains
    Assert.assertThrows(IllegalArgumentException.class, () -> { check.checkAndSet(-1); });
    Assert.assertThrows(IllegalArgumentException.class, () -> { check.checkAndSet(20); });
    // Check normal
    check.checkAndSet(5);
    Assert.assertFalse(check.empty());
    Assert.assertFalse(check.unset());
    Assert.assertTrue(5 == check.value());
  }

}
