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

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.protobuf.generated.HBaseProtos;
import org.apache.aries.common.BoolParameter;
import org.apache.aries.common.Parameter;
import org.apache.aries.common.StringArrayParameter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RenameTable extends AbstractHBaseToy {

  private Parameter<String[]> old_table_names =
      StringArrayParameter.newBuilder("rt.old_table_names").setRequired()
                          .setDescription("Old tables's names").opt();
  private Parameter<String[]> new_table_names =
      StringArrayParameter.newBuilder("rt.new_table_names").setRequired()
                          .setDescription("New tables's names").opt();
  private Parameter<Boolean> skip_flush =
      BoolParameter.newBuilder("rt.snapshot_skip_flush", false)
                   .setDescription("Whether skip flush when snapshot table").opt();

  @Override protected void requisite(List<Parameter> requisites) {
    requisites.add(old_table_names);
    requisites.add(new_table_names);
    requisites.add(skip_flush);
  }

  @Override protected void midCheck() {
    if (old_table_names.value().length != new_table_names.value().length) {
      throw new RuntimeException("table mapping is not one to one");
    }
  }

  @Override protected void buildToy(ToyConfiguration configuration) throws Exception {
    super.buildToy(configuration);

    String[] old_tables = old_table_names.value();
    String[] new_tables = new_table_names.value();
    for (int i = 0; i < old_tables.length; i++) {
      table_mapping.put(old_tables[i], new_tables[i]);
    }
  }

  private Admin admin;
  private Map<String, String> table_mapping = new HashMap<>();

  @Override protected int haveFun() throws Exception {
    admin = connection.getAdmin();
    table_mapping.forEach((o, n) -> {
      TableName name = TableName.valueOf(o);
      try {
        admin.snapshot(name.getQualifierAsString(), name,
            skip_flush.value() ? HBaseProtos.SnapshotDescription.Type.SKIPFLUSH : HBaseProtos.SnapshotDescription.Type.FLUSH);
        admin.cloneSnapshot(name.getQualifierAsString(), TableName.valueOf(n), true);
      } catch (IOException e) {
        LOG.warning("Failed to snapshot " + name);
      }
    });
    return 0;
  }

  @Override protected void destroyToy() throws Exception {
    super.destroyToy();
    admin.close();
  }

}
