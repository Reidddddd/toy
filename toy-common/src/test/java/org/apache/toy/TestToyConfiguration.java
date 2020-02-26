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

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class TestToyConfiguration {

  private static File toy_conf_file;
  private static ToyConfiguration toy_properties;

  public enum TEST {
    DEFAULT, SPECIFIC
  }

  @BeforeClass public static void setup() throws Exception {
    toy_conf_file = new File("./toy.properties");
    Assert.assertTrue(toy_conf_file.createNewFile());
    Assert.assertNotNull(toy_conf_file);

    BufferedWriter writer = new BufferedWriter(new FileWriter(toy_conf_file));
    writer.write("a=1");                  writer.newLine();
    writer.write("b=foo");                writer.newLine();
    writer.write("c=what,about,strings"); writer.newLine();
    writer.write("d=true");               writer.newLine();
    writer.write("e=1.2");                writer.newLine();
    writer.write("f=0.99");               writer.newLine();
    writer.write("g=1111111111111");      writer.newLine();
    writer.write("h=SPECIFIC");           writer.newLine();
    writer.close();

    toy_properties = new ToyConfiguration(toy_conf_file.toPath());
  }

  @AfterClass public static void teardown() {
    Assert.assertTrue(toy_conf_file.exists());
    if (toy_conf_file.exists()) {
      Assert.assertTrue(toy_conf_file.delete());
    }
  }

  @Test public void testGetInt() {
    Assert.assertEquals(1, toy_properties.getInt("a"));
  }

  @Test public void testGetString() {
    Assert.assertEquals("foo", toy_properties.get("b"));
  }

  @Test public void testGetStrings() {
    String[] values = toy_properties.getStrings("c", ",");
    Assert.assertNotNull(values);
    Assert.assertEquals(3, values.length);
    Assert.assertEquals("what", values[0]);
    Assert.assertEquals("about", values[1]);
    Assert.assertEquals("strings", values[2]);
  }

  @Test public void testGetBoolean() {
    Assert.assertTrue(toy_properties.getBoolean("d", Constants.UNSET_FALSE));
  }

  @Test public void testGetDouble() {
    Assert.assertEquals(0, Double.compare(1.2D, toy_properties.getDouble("e")));
  }

  @Test public void testGetFloat() {
    Assert.assertEquals(0, Float.compare(0.99F, toy_properties.getFloat("f")));
  }

  @Test public void testGetLong() {
    Assert.assertEquals(0, Long.compare(1111111111111L, toy_properties.getLong("g")));
  }

  @Test public void testGetEnum() {
    Assert.assertEquals(TEST.SPECIFIC, toy_properties.getEnum("h", TEST.DEFAULT));
  }

  @Test public void testUnset() {
    Assert.assertEquals(Constants.UNSET_INT, toy_properties.getInt("u"));
    Assert.assertEquals(Constants.UNSET_LONG, toy_properties.getLong("v"));
    Assert.assertEquals(0, Float.compare(Constants.UNSET_FLOAT, toy_properties.getFloat("w")));
    Assert.assertEquals(0, Double.compare(Constants.UNSET_DOUBLE, toy_properties.getDouble("x")));
    Assert.assertEquals(Constants.UNSET_STRING, toy_properties.get("y"));
    Assert.assertSame(Constants.UNSET_STRINGS, toy_properties.getStrings("z", ","));
    Assert.assertSame(Constants.UNSET_TRUE, toy_properties.getBoolean("t", Constants.UNSET_TRUE));
  }

  @Test public void testContainsKey() {
    Assert.assertTrue(toy_properties.containsKey("a"));
    Assert.assertFalse(toy_properties.containsKey("x"));
  }

}
