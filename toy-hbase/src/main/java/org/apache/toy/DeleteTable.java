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

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.toy.common.Parameter;
import org.apache.toy.common.StringArrayParameter;

import java.util.List;

public class DeleteTable extends AbstractHBaseToy {
  private final Parameter<String[]> tables =
      StringArrayParameter.newBuilder("dt.table_name").setRequired().setDescription("tables's names to be dropped")
                          .addConstraint(v -> v.length > 0).opt();

  @Override protected void requisite(List<Parameter> requisites) {
    requisites.add(tables);
  }

  @Override protected void buildToy(ToyConfiguration configuration) throws Exception {
    super.buildToy(configuration);
    admin = connection.getAdmin();
  }

  private Admin admin;

  @Override protected int haveFun() throws Exception {
    for (String table : tables.value()) {
      TableName name = TableName.valueOf(table);
      if (admin.tableExists(name)) {
        admin.disableTable(name);
        admin.deleteTable(name);
      }
    }
    return RETURN_CODE.SUCCESS.code();
  }

  @Override protected void destroyToy() throws Exception {
    super.destroyToy();
    admin.close();
  }

}
