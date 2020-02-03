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

import org.junit.*;
import org.junit.rules.ExpectedException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class TestIllegalConfiguration {

  private File toy_site_conf;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setup() throws Exception {
    toy_site_conf = new File("./toy-site.conf");
    Assert.assertTrue(toy_site_conf.createNewFile());
    Assert.assertNotNull(toy_site_conf);

    BufferedWriter writer = new BufferedWriter(new FileWriter(toy_site_conf));
    writer.write("a=1");                  writer.newLine();
    writer.write("b=foo");                writer.newLine();
    writer.write("c=what,about,strings"); writer.newLine();
    writer.write("d=true");               writer.newLine();
    writer.write("e=SPECIFIC");           writer.newLine();
    writer.close();
  }

  @After
  public void teardown() {
    if (toy_site_conf.exists()) {
      Assert.assertTrue(toy_site_conf.delete());
    }
  }

  @Test
  public void testIllegalConfiguration() throws Exception {
    expectedException.expect(RuntimeException.class);
    expectedException.expectMessage("Unauthorized toy-site.conf");
    ConfigurationFactory.createJavaConfiguration(".");
  }

}
