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
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;

import java.util.List;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public abstract class HDFSBenchmark extends AbstractBenchmarkToy {

  private final Parameter<String> working_directory =
      StringParameter.newBuilder("bm.hdfs.working_directory").setDefaultValue("/benchmark").setDescription("Working directory for benchwork.").opt();
  private final Parameter<String> toy_configuration =
      StringParameter.newBuilder("bm.hdfs.toy_configuration_dir").setRequired().setDescription("Toy configuration directory").opt();

  protected FileSystem file_system;
  protected Configuration conf;
  protected Path work_dir;

  protected ToyConfiguration toy_conf;

  @Override
  protected void decorateOptions(ChainedOptionsBuilder options_builder) {
    options_builder.jvmArgs("-D" + toy_configuration.key() + "=" + toy_configuration.value());
  }

  @Setup
  public void setup() throws Exception {
    conf = ConfigurationFactory.createHDFSConfiguration(ToyConfiguration.create(System.getProperty(toy_configuration.key())));
    file_system = FileSystem.get(conf);
    work_dir = new Path(working_directory.value());
    if (!file_system.exists(work_dir)) {
      LOG.info("Creating directory " + work_dir);
      file_system.mkdirs(work_dir);
    }
    LOG.info("Working directory is " + work_dir);
  }

  @Override protected void requisite(List<Parameter> requisites) {
    super.requisite(requisites);
    requisites.add(working_directory);
    requisites.add(toy_configuration);
  }

  @Override
  protected void exampleConfiguration() {
    super.exampleConfiguration();
    example(working_directory.key(), working_directory.defvalue());
    example(toy_configuration.key(), "/path/to/dir");
  }

  @Override protected void buildToy(ToyConfiguration configuration) throws Exception {
    super.buildToy(configuration);
    toy_conf = configuration;
  }

  @Override protected void destroyToy() throws Exception {
    super.destroyToy();
  }

  @TearDown
  public void teardown() throws Exception {
    file_system.close();
  }

}
