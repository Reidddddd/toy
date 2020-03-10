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

import org.apache.toy.common.Parameter;
import org.apache.toy.common.StringParameter;

import java.sql.Statement;
import java.util.List;

public class CreatePhoenixTable extends AbstractPhoenixToy {
  private final Parameter<String> sql =
      StringParameter.newBuilder("cpt.create_table_sql").setRequired().setDescription("create table sql").opt();

  @Override protected void requisite(List<Parameter> requisites) {
    requisites.add(sql);
  }

  @Override protected int haveFun() throws Exception {
    System.out.println("What the sql looks like: " + sql.value());
    Statement statement = connection.createStatement();
    statement.execute(sql.value());
    return RETURN_CODE.SUCCESS.code();
  }

}
