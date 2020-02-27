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

import org.apache.toy.common.BoolParameter;
import org.apache.toy.common.EnumParameter;
import org.apache.toy.common.IntParameter;
import org.apache.toy.common.Parameter;
import org.apache.toy.common.StringArrayParameter;
import org.apache.toy.common.StringParameter;
import org.junit.Assert;
import org.junit.Test;

public class TestParameter {

  @Test public void testStringValue() {
    Parameter<String> sp = StringParameter.newBuilder("k").setDefaultValue("v").setRequired().opt();
    Assert.assertEquals(String.class, sp.type());
    Assert.assertFalse(sp.empty());
    Assert.assertTrue(sp.required());
    Assert.assertEquals("v", sp.value());
  }

  @Test public void testIntValue() {
    Parameter<Integer> ip = IntParameter.newBuilder("i").setDefaultValue(Integer.MAX_VALUE).opt();
    Assert.assertEquals(Integer.class, ip.type());
    Assert.assertFalse(ip.empty());
    Assert.assertFalse(ip.required());
    Assert.assertEquals(Integer.MAX_VALUE, (int)ip.value());
  }

  @Test public void testEnumValue() {
    Parameter<Enum> ep = EnumParameter.newBuilder("e", TestToyConfiguration.TEST.DEFAULT,
        TestToyConfiguration.TEST.class).opt();
    Assert.assertEquals(TestToyConfiguration.TEST.class, ep.type());
    Assert.assertFalse(ep.empty());
    Assert.assertFalse(ep.required());
    Assert.assertEquals(TestToyConfiguration.TEST.DEFAULT, ep.value());
  }

  @Test public void testStringsValue() {
    Parameter<String[]> sap = StringArrayParameter.newBuilder("sa").setDefaultValue(new String[] {"x", "y", "z"}).opt();
    Assert.assertEquals(String[].class, sap.type());
    Assert.assertFalse(sap.empty());
    Assert.assertFalse(sap.required());
    Assert.assertEquals(3, sap.value().length);
    Assert.assertEquals("x", sap.value()[0]);
    Assert.assertEquals("y", sap.value()[1]);
    Assert.assertEquals("z", sap.value()[2]);
  }

  @Test public void testBooleanValue() {
    Parameter<Boolean> bp = BoolParameter.newBuilder("b", false).opt();
    Assert.assertEquals(Boolean.class, bp.type());
    Assert.assertFalse(bp.empty());
    Assert.assertFalse(bp.required());
    Assert.assertFalse(bp.value());
  }

  @Test public void testEmpty() {
    Parameter<String> empty = StringParameter.newBuilder("empty").opt();
    Assert.assertTrue(empty.empty());
  }

  @Test public void testSet() {
    Parameter<Integer> set = IntParameter.newBuilder("set").opt();
    Assert.assertTrue(set.empty());
    set.setValue(Integer.MIN_VALUE);
    Assert.assertFalse(set.empty());
    Assert.assertEquals(Integer.MIN_VALUE, (int)set.value());
  }

  @Test public void testCheck() {
    Parameter<Integer> check = IntParameter.newBuilder("check").addConstraint(v -> v > 1).addConstraint(v -> v <= 10).opt();
    // Check null
    Assert.assertTrue(check.empty());
    // Check value doesn't satisfy constrains
    Assert.assertThrows(IllegalArgumentException.class, () -> check.checkAndSet(-1));
    Assert.assertThrows(IllegalArgumentException.class, () -> check.checkAndSet(20));
    // Check normal
    check.checkAndSet(5);
    Assert.assertFalse(check.empty());
    Assert.assertEquals(5, (int) check.value());
  }

}
