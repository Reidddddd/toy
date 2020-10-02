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
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;

import java.util.List;

public abstract class HDFSBenchmark extends BasicBenchmarkToy {

  private final Parameter<String> working_directory =
      StringParameter.newBuilder("bm.hdfs.working_directory").setDefaultValue("/benchmark").setDescription("Working directory for benchwork.").opt();


  protected DistributedFileSystem file_system = new DistributedFileSystem();;
  protected Configuration conf;
  protected Path work_dir;

  @Setup(Level.Trial)
  public void setup() throws Exception {
    super.setup();
    conf = ConfigurationFactory.createHDFSConfiguration(toy_conf);
    file_system.initialize(FileSystem.getDefaultUri(conf), conf);
    work_dir = new Path(working_directory.value());
    if (!file_system.exists(work_dir)) {
      file_system.mkdirs(work_dir);
    }
  }

  @Setup(Level.Invocation)
  public void reinit() throws Exception {
    injectConfiguration();
    file_system.initialize(FileSystem.getDefaultUri(conf), conf);
  }

  @Override
  protected void requisite(List<Parameter> requisites) {
    super.requisite(requisites);
    requisites.add(working_directory);
  }

  @TearDown(Level.Trial)
  public void teardown() throws Exception {
    file_system.delete(work_dir, true);
    file_system.close();
  }

  abstract void injectConfiguration();

}
