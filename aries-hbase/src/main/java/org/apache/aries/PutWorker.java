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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.TableNotFoundException;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.BufferedMutator;
import org.apache.hadoop.hbase.client.BufferedMutatorParams;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.aries.common.Constants;
import org.apache.aries.common.IntParameter;
import org.apache.aries.common.LongParameter;
import org.apache.aries.common.Parameter;
import org.apache.aries.common.StringParameter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PutWorker extends AbstractHBaseToy {

  private final Parameter<Integer> num_connections =
      IntParameter.newBuilder("pw.num_connections").setRequired()
                  .setDescription("number of connections used for put")
                  .addConstraint(v -> v > 0).opt();
  private final Parameter<String> table_name =
      StringParameter.newBuilder("pw.target_table").setRequired()
                     .setDescription("table that data will be put in").opt();
  private final Parameter<String> family =
      StringParameter.newBuilder("pw.target_family")
                     .setDescription("a family that belongs to the target_table, and wanted to be put in data")
                     .setRequired().opt();
  private final Parameter<Long> buffer_size =
      LongParameter.newBuilder("pw.buffer_size").setDefaultValue(Constants.ONE_MB)
                   .setDescription("buffer size for batch put").opt();

  private Admin admin;
  private ExecutorService service;
  private volatile boolean running = true;
  private TableName table;
  private final Object mutex = new Object();

  @Override
  protected void requisite(List<Parameter> requisites) {
    requisites.add(num_connections);
    requisites.add(table_name);
    requisites.add(family);
    requisites.add(buffer_size);
  }

  @Override
  protected void buildToy(ToyConfiguration configuration) throws Exception {
    super.buildToy(configuration);
    table = TableName.valueOf(table_name.value());
    admin = connection.getAdmin();
    if (!admin.tableExists(table)) {
      throw new TableNotFoundException(table);
    }

    service = Executors.newFixedThreadPool(num_connections.value());
    for (int i = 0; i < num_connections.value(); i++) {
      service.submit(new Worker(configuration));
    }

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      synchronized (mutex) {
        mutex.notify();
      }
    }));
  }

  @Override
  protected int haveFun() throws Exception {
    synchronized (mutex) {
      mutex.wait();
      running = false;
    }
    return 0;
  }

  @Override
  protected void destroyToy() throws Exception {
    super.destroyToy();
    admin.close();
    service.shutdown();
  }

  private static String char_list = "abcdefghijklmnopqrstuvwxyz0123456789";
  private static String generateRandomString(int size) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < size; i++) {
      builder.append(char_list.charAt((int)(Math.random() * char_list.length())));
    }
    return builder.toString();
  }

  class Worker implements Runnable {

    Connection connection;

    Worker(ToyConfiguration conf) throws IOException {
      connection = createConnection(conf);
      LOG.info("Connection created " + connection);
    }

    Connection createConnection(ToyConfiguration conf) throws IOException {
      Configuration hbase_conf = ConfigurationFactory.createHBaseConfiguration(conf);
      return ConnectionFactory.createConnection(hbase_conf);
    }

    @Override
    public void run() {
      BufferedMutator mutator = null;
      BufferedMutatorParams param = new BufferedMutatorParams(table);
      param.writeBufferSize(buffer_size.value());
      try {
        mutator = connection.getBufferedMutator(param);
        while (running) {
          Put put = new Put(Bytes.toBytes(generateRandomString(10)));
          put.addColumn(
              Bytes.toBytes(family.value()),
              Bytes.toBytes("q"),
              Bytes.toBytes(generateRandomString(22))
          );
          mutator.mutate(put);
        }
      } catch (IOException e) {
        LOG.warning("Error occured " + e.getMessage());
      }
    }

  }

}
