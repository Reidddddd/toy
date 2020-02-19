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

import org.apache.hadoop.conf.Configuration;
import org.apache.toy.AbstractPhoenixToy;
import org.apache.toy.common.Parameter;

import java.util.List;

public class CreatePTable extends AbstractPhoenixToy {
  private final Parameter<String> namespace =
      Parameter.<String>newBuilder()
               .setKey("cpt.namespace").setDescription("Table's namespace")
               .setType(String.class)
               .opt();
  private final Parameter<String> tablename =
      Parameter.<String>newBuilder()
               .setKey("cpt.tablename").setDescription("Table's name")
               .setType(String.class).setRequired(true)
               .opt();

  @Override
  protected void requisite(List<Parameter> requisites) {

  }

  @Override
  protected void buildToy(Configuration configuration) throws Exception {

  }

  @Override
  protected int haveFun() throws Exception {
    return 0;
  }

  @Override
  protected void destroyToy() throws Exception {

  }

}
