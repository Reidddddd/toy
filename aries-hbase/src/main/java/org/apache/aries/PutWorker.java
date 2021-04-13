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

import org.apache.aries.common.EnumParameter;
import org.apache.aries.common.ToyUtils;
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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class PutWorker extends AbstractHBaseToy {

  private final Parameter<Integer> num_connections =
      IntParameter.newBuilder("pw.num_connections").setRequired()
                  .setDescription("Number of connections used for put")
                  .addConstraint(v -> v > 0).opt();
  private final Parameter<String> table_name =
      StringParameter.newBuilder("pw.target_table").setRequired()
                     .setDescription("A table that data will be put in").opt();
  private final Parameter<String> family =
      StringParameter.newBuilder("pw.target_family")
                     .setDescription("A family that belongs to the target_table, and wanted to be put in data")
                     .setRequired().opt();
  private final Parameter<Long> buffer_size =
      LongParameter.newBuilder("pw.buffer_size").setDefaultValue(Constants.ONE_MB)
                   .setDescription("Buffer size in bytes for batch put").opt();
  private final Parameter<Integer> running_time =
      IntParameter.newBuilder("pw.running_time").setDescription("How long this application run (in seconds").opt();
  private final Parameter<Enum> value_kind =
      EnumParameter.newBuilder("pw.value_kind", VALUE_KIND.FIXED, VALUE_KIND.class)
                   .setDescription("Value is fixed or random generated").opt();
  private final Parameter<Integer> running_threads =
      IntParameter.newBuilder("pw.running_threads").setDefaultValue(1)
          .setDescription("How many threads use one connection.")
          .addConstraint(v -> v > 0).opt();

  enum VALUE_KIND {
    RANDOM, FIXED
  }

  private Admin admin;
  private ExecutorService service;
  private volatile boolean running = true;
  private TableName table;
  private final Object mutex = new Object();
  private VALUE_KIND kind;
  private AtomicLong totalRows = new AtomicLong(0);

  @Override
  protected void requisite(List<Parameter> requisites) {
    requisites.add(num_connections);
    requisites.add(table_name);
    requisites.add(family);
    requisites.add(buffer_size);
    requisites.add(running_time);
    requisites.add(value_kind);
    requisites.add(running_threads);
  }

  @Override
  protected void exampleConfiguration() {
    example(num_connections.key(), "3");
    example(table_name.key(), "table:for_put");
    example(family.key(), "f");
    example(buffer_size.key(), "1024");
    example(running_time.key(), "300");
    example(value_kind.key(), "FIXED");
    example(running_threads.key(), "10");
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
    Worker[] workers = new Worker[num_connections.value()];
    for (int i = 0; i < num_connections.value(); i++) {
      workers[i] = new Worker(configuration);
      service.submit(workers[i]);
    }

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      synchronized (mutex) {
        mutex.notify();
      }
    }));

    if (!running_time.empty()) {
      new Timer().schedule(new TimerTask() {
        @Override
        public void run() {
          try {
            TimeUnit.SECONDS.sleep(running_time.value());
          } catch (InterruptedException e) {
            // Ignore
          } finally {
            synchronized (mutex) {
              mutex.notify();
            }
          }
        }
      }, 0);
    }

    kind = (VALUE_KIND) value_kind.value();
  }

  @Override
  protected int haveFun() throws Exception {
    synchronized (mutex) {
      mutex.wait();
      running = false;
    }
    service.awaitTermination(30, TimeUnit.SECONDS);
    LOG.info("Total wrote " + totalRows.get() + " rows in " + running_time.value() + " seconds.");
    LOG.info("Avg " + (double) (totalRows.get()) / running_time.value());
    LOG.info("Existing.");
    return 0;
  }

  @Override
  protected void destroyToy() throws Exception {
    super.destroyToy();
    admin.close();
  }

  @Override protected String getParameterPrefix() {
    return "pw";
  }

  class Worker implements Runnable {

    Connection connection;
    AtomicLong numberOfRows = new AtomicLong(0);

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
      ExecutorService service = Executors.newFixedThreadPool(running_threads.value());
      try {
        mutator = connection.getBufferedMutator(param);
        for (int i = 0; i < running_threads.value(); i++) {
          BufferedMutator finalMutator = mutator;
          service.execute(() -> {
            while (running) {
              String k = ToyUtils.generateRandomString(10);
              byte[] value = (kind == VALUE_KIND.FIXED) ?
                  ToyUtils.generateBase64Value(k) :
                  Bytes.toBytes(ToyUtils.generateRandomString(22));
              Put put = new Put(Bytes.toBytes(k));
              put.addColumn(
                  Bytes.toBytes(family.value()),
                  Bytes.toBytes("q"),
                  value
              );
              try {
                finalMutator.mutate(put);
              } catch (IOException e) {
                LOG.warning("Error occured " + e.getMessage());
              }
              numberOfRows.incrementAndGet();
            }
          });
        }
        service.shutdown();
        service.awaitTermination(running_time.value(), TimeUnit.SECONDS);
        mutator.flush();
        mutator.close();
      } catch (IOException | InterruptedException e) {
        LOG.warning("Error occured " + e.getMessage());
      } finally {
        totalRows.addAndGet(numberOfRows.get());
      }
    }

  }

}
