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
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public abstract class BasicBenchmarkToy extends AbstractBenchmarkToy {

  private final Parameter<String> toy_configuration =
      StringParameter.newBuilder("bm.toy_configuration_dir").setRequired().setDescription("Toy configuration directory").opt();

  @SuppressWarnings("rawtypes")
  private final List<Parameter> parameters = new LinkedList<>();

  protected ToyConfiguration toy_conf;
  protected boolean inited = false;

  @Override
  protected void decorateOptions(ChainedOptionsBuilder options_builder) {
    options_builder.jvmArgs("-D" + toy_configuration.key() + "=" + toy_configuration.value());
  }

  @Setup(Level.Trial)
  public void setup() throws Exception {
    toy_conf = ToyConfiguration.create(System.getProperty(toy_configuration.key()));
    requisite(parameters);
    preCheck(toy_conf, parameters);
    midCheck();
    printParameters(toy_conf, getParameterPrefix());
    inited = true;
  }

  @Override
  protected void requisite(List<Parameter> requisites) {
    super.requisite(requisites);
    requisites.add(toy_configuration);
  }

}
