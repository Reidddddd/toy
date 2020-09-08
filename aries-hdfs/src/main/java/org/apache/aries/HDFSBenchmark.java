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

package org.apache.aries;

import org.apache.aries.common.Parameter;
import org.apache.aries.common.StringParameter;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;

import java.util.List;

public abstract class HDFSBenchmark extends BasicBenchmarkToy {

  private final Parameter<String> working_directory =
      StringParameter.newBuilder("bm.hdfs.working_directory").setDefaultValue("/benchmark").setDescription("Working directory for benchwork.").opt();


  protected FileSystem file_system;
  protected Configuration conf;
  protected Path work_dir;

  protected ToyConfiguration toy_conf;

  @Setup
  public void setup() throws Exception {
    super.setup();
    conf = ConfigurationFactory.createHDFSConfiguration(toy_conf);
    file_system = FileSystem.get(conf);
    work_dir = new Path(working_directory.value());
    if (!file_system.exists(work_dir)) {
      LOG.info("Creating directory " + work_dir);
      file_system.mkdirs(work_dir);
    }
    LOG.info("Working directory is " + work_dir);
  }

  @Override
  protected void addParameters(List<Parameter> parameters) {
    super.addParameters(parameters);
    parameters.add(working_directory);
  }

  @TearDown
  public void teardown() throws Exception {
    file_system.close();
  }

}
