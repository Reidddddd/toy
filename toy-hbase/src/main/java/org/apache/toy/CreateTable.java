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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableExistsException;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.toy.common.Parameter;

import java.util.List;

/**
 * A class for creating table.
 */
public class CreateTable extends AbstractHBaseToy {

  private final Parameter<String> table_name =
      Parameter.<String>newBuilder().setKey("table_name").setRequired(true)
                                    .setType(String.class).setDescription("table name")
                                    .opt();
  private final Parameter<String[]> families =
      Parameter.<String[]>newBuilder().setKey("families_name").setRequired(true)
                                      .setType(String[].class).setDescription("A family or families delimited by ','")
                                      .opt();

  private TableName table;
  private Connection connection;
  private Admin admin;

  @Override
  protected void requisite(List<Parameter<?>> requisites) {
    requisites.add(table_name);
    requisites.add(families);
  }

  @Override
  protected void buildToy(Configuration configuration) throws Exception {
    table = TableName.valueOf(table_name.value());
    connection = ConnectionFactory.createConnection(configuration);
    admin = connection.getAdmin();
  }

  @Override
  public int haveFun() throws Exception {
    if (admin.tableExists(table)) {
      throw new TableExistsException(table);
    }

    HTableDescriptor descriptor = buildTableDescriptor();
    SplitPolicy policy = buildSplitPolicy();
    admin.createTable(descriptor, policy.getSplitsKeys());
    return RETURN_CODE.SUCCESS.code();
  }

  private HTableDescriptor buildTableDescriptor() {
    HTableDescriptor descriptor = new HTableDescriptor(table);
    return descriptor;
  }

  private SplitPolicy buildSplitPolicy() {
    return new SplitPolicy() {

      @Override
      public byte[][] getSplitsKeys() {
        return new byte[0][];
      }

    };
  }

  @Override
  protected void destroyToy() throws Exception {
    admin.close();
    connection.close();
  }

  private interface SplitPolicy {

    byte[][] getSplitsKeys();

  }

}
