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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.FileNotFoundException;
import java.nio.file.NotDirectoryException;

public class TestConfigurationFactory {

  @Rule
  public ExpectedException exceptionRule = ExpectedException.none();

  @Test public void testJavaToyFileNotFoundException() throws Exception {
    exceptionRule.expect(FileNotFoundException.class);
    exceptionRule.expectMessage("toy-site.conf for Java toy doesn't exist");
    ConfigurationFactory.createJavaConfiguration(System.getProperty("user.dir"));
  }

  @Test public void testJavaToyNotDirectoryException() throws Exception {
    exceptionRule.expect(NotDirectoryException.class);
    exceptionRule.expectMessage("maybe not a directory?");
    ConfigurationFactory.createJavaConfiguration("./TestConfigurationFactory.java");
  }

  @Test public void testHBaseToyFileNotFoundException() throws Exception {
    exceptionRule.expect(FileNotFoundException.class);
    exceptionRule.expectMessage("hbase-site.xml doesn't exist");
    ConfigurationFactory.createHBaseConfiguration(System.getProperty("user.dir"));
  }

  @Test public void testHBaseToyNotDirectoryException() throws Exception {
    exceptionRule.expect(NotDirectoryException.class);
    exceptionRule.expectMessage("maybe not a directory?");
    ConfigurationFactory.createHBaseConfiguration("./TestConfigurationFactory.java");
  }

}
