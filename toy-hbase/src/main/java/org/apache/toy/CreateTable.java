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
@SuppressWarnings("rawtypes")
public class CreateTable extends AbstractHBaseToy {

  private final Parameter<String> table_name =
      Parameter.<String>newBuilder()
               .setKey("table_name").setRequired(true)
               .setType(String.class).setDescription("table name")
               .opt();
  private final Parameter<String[]> families =
      Parameter.<String[]>newBuilder()
               .setKey("families_name").setRequired(true)
               .setType(String[].class).setDescription("A family or families delimited by ','")
               .opt();
  private final Parameter<Enum> split_algorithm =
      Parameter.<Enum>newBuilder()
               .setKey("split_algorithm").setDefaultValue(ALGORITHM.NONE)
               .setType(ALGORITHM.class).setDescription("Split algorithm, so far suport HEX and NUM")
               .opt();
  private final Parameter<Integer> hex_split_regions =
      Parameter.<Integer>newBuilder()
               .setKey("hex_split_regions").setType(Integer.class)
               .setDescription("Number of regions expecting when using hex split algorithm, upper bound is 256")
               .addConstraint(Parameter.Condition.GREATER_EQUAL, 1)
               .addConstraint(Parameter.Condition.LESS_EQUAL, 256)
               .opt();
  private final Parameter<Integer> num_split_regions =
      Parameter.<Integer>newBuilder()
               .setKey("num_split_regions").setType(Integer.class)
               .addConstraint(Parameter.Condition.GREATER_EQUAL, 1)
               .addConstraint(Parameter.Condition.LESS_EQUAL, 1000)
               .setDescription("Number of regions expecting when using number split algorithm, left padded with zeros")
               .opt();

  private TableName table;
  private Connection connection;
  private Admin admin;
  private SplitAlgorithm split;

  @Override
  protected void requisite(List<Parameter> requisites) {
    requisites.add(table_name);
    requisites.add(families);
    requisites.add(split_algorithm);
    requisites.add(hex_split_regions);
    requisites.add(num_split_regions);
  }

  @Override
  protected void buildToy(Configuration configuration) throws Exception {
    table = TableName.valueOf(table_name.value());
    connection = ConnectionFactory.createConnection(configuration);
    admin = connection.getAdmin();
    split = buildSplitAlgorithm(split_algorithm.value());
  }

  @Override
  public int haveFun() throws Exception {
    if (admin.tableExists(table)) {
      throw new TableExistsException(table);
    }

    HTableDescriptor descriptor = buildTableDescriptor();
    admin.createTable(descriptor, split.getSplitsKeys());
    return RETURN_CODE.SUCCESS.code();
  }

  private HTableDescriptor buildTableDescriptor() {
    HTableDescriptor descriptor = new HTableDescriptor(table);
    return descriptor;
  }

  private SplitAlgorithm buildSplitAlgorithm(Enum raw_algorithm) {
    ALGORITHM algorithm = (ALGORITHM) raw_algorithm;
    switch (algorithm) {
      case HEX:
        return new HexSplitAlgorithm(hex_split_regions.value());
      case NUMBER:
        return new NumberSplitAlgorithm(num_split_regions.value());
      default:
        return new NoneSplitAlgorithm();

    }
  }

  enum ALGORITHM {
    NONE, HEX, NUMBER
  }

  @Override
  protected void destroyToy() throws Exception {
    admin.close();
    connection.close();
  }

  private interface SplitAlgorithm {

    byte[][] getSplitsKeys();

  }

  private class NoneSplitAlgorithm implements SplitAlgorithm {

    @Override public byte[][] getSplitsKeys() {
      return null;
    }

  }

  private class NumberSplitAlgorithm implements SplitAlgorithm {

    NumberSplitAlgorithm(int expect_splits) {

    }

    @Override public byte[][] getSplitsKeys() {
      return new byte[0][];
    }

  }

  private class HexSplitAlgorithm implements SplitAlgorithm {

    HexSplitAlgorithm(int expect_splits) {
    }

    @Override public byte[][] getSplitsKeys() {
      return new byte[0][];
    }

  }

}
