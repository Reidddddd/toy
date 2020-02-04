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

public class TestConfiguration {

  private static File toy_site_conf;
  private static Configuration configuration;

  public enum TEST {
    DEFAULT, SPECIFIC
  }

  @BeforeClass public static void setup() throws Exception {
    toy_site_conf = new File("./toy-site.conf");
    Assert.assertTrue(toy_site_conf.createNewFile());
    Assert.assertNotNull(toy_site_conf);

    BufferedWriter writer = new BufferedWriter(new FileWriter(toy_site_conf));
    writer.write("# Copyright (c) R.C");  writer.newLine();
    writer.write("a=1");                  writer.newLine();
    writer.write("b=foo");                writer.newLine();
    writer.write("c=what,about,strings"); writer.newLine();
    writer.write("d=true");               writer.newLine();
    writer.write("e=SPECIFIC");           writer.newLine();
    writer.close();
    configuration = ConfigurationFactory.createJavaConfiguration(".");
  }

  @AfterClass public static void teardown() {
    if (toy_site_conf.exists()) {
      Assert.assertTrue(toy_site_conf.delete());
    }
  }

  @Test public void testGetInt() {
    Assert.assertEquals(1, configuration.getInt("a"));
  }

  @Test public void testGetString() {
    Assert.assertEquals("foo", configuration.get("b"));
  }

  @Test public void testGetStrings() {
    String[] values = configuration.getStrings("c");
    Assert.assertEquals(3, values.length);
    Assert.assertEquals("what", values[0]);
    Assert.assertEquals("about", values[1]);
    Assert.assertEquals("strings", values[2]);
  }

  @Test public void testGetBoolean() {
    Assert.assertEquals(Boolean.TRUE, configuration.getBoolean("d", Boolean.FALSE));
  }

  @Test public void testGetEnum() {
    Enum test = configuration.getEnum("e", TEST.DEFAULT);
    Assert.assertTrue(test instanceof TEST);
    Assert.assertEquals(TEST.SPECIFIC, test);
  }

  @Test public void testCheckKey() {
    Assert.assertTrue(configuration.checkKey("a"));
    Assert.assertTrue(configuration.checkKey("b"));
    Assert.assertTrue(configuration.checkKey("c"));
    Assert.assertTrue(configuration.checkKey("d"));
    Assert.assertTrue(configuration.checkKey("e"));
    Assert.assertFalse(configuration.checkKey("f"));
  }

}
