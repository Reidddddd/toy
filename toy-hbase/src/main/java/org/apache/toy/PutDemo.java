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
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.BufferedMutator;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.PrintStream;

public class PutDemo extends AbstractHBaseToy {

  public PutDemo() {}

  @Override
  public int howToPlay(PrintStream out) {
    return RETURN_CODE.HELP.code();
  }

  @Override
  public void preCheck(Configuration configuration) throws Exception {

  }

  public int haveFun(Configuration configuration) throws Exception {
    Connection connection = ConnectionFactory.createConnection(configuration);
    TableName tableName = TableName.valueOf("toy", "hbase");
    Table table = connection.getTable(tableName);
    BufferedMutator bm = connection.getBufferedMutator(tableName);
    Put put = new Put(Bytes.toBytes("putdemo_toy"));
    put.addColumn(Bytes.toBytes("p"), Bytes.toBytes("q"), Bytes.toBytes("putdemo"));
    bm.mutate(put);
    bm.flush();
    table.put(put);
    table.close();
    connection.close();
    return 0;
  }

}
