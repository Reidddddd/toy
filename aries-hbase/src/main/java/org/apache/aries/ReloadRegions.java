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

import org.apache.aries.common.IntParameter;
import org.apache.aries.common.Parameter;
import org.apache.aries.common.StringParameter;
import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.protobuf.ProtobufUtil;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReloadRegions extends AbstractHBaseToy {

  private final Parameter<String> target_server =
      StringParameter.newBuilder("rr.target_server").setRequired()
          .setDescription("Regions on this server will be unloaded, then reloaded. Please use server:port format").opt();
  private final Parameter<String> temp_server =
      StringParameter.newBuilder("rr.temp_server").setRequired()
          .setDescription("This server will be used for store the regions from target server temporarily.").opt();
  private final Parameter<Integer> thread_pool_size =
      IntParameter.newBuilder("rr.threads_for_move_regions").setDefaultValue(8).setDescription("Number of threads for moving regions.").opt();

  @Override protected String getParameterPrefix() {
    return "rr";
  }

  @Override protected void requisite(List<Parameter> requisites) {
    requisites.add(target_server);
    requisites.add(temp_server);
    requisites.add(thread_pool_size);
  }

  @Override protected void exampleConfiguration() {
    example(target_server.key(), "target_server.com:5678");
    example(temp_server.key(), "temp_server.com:5678");
    example(thread_pool_size.key(), "8");
  }

  Admin admin;
  ServerName target;
  ServerName temp;
  List<HRegionInfo> target_regions;
  ExecutorService pool;
  CyclicBarrier barrier;

  @Override protected void buildToy(ToyConfiguration configuration) throws Exception {
    super.buildToy(configuration);
    admin = connection.getAdmin();
    target = findServer(target_server.value());
    target_regions = ProtobufUtil.getOnlineRegions(HConnectionManager.getConnection(connection.getConfiguration()).getAdmin(target));
    barrier = new CyclicBarrier(target_regions.size() + 1);
    pool = Executors.newFixedThreadPool(thread_pool_size.value());
  }

  @Override protected int haveFun() throws Exception {
    unload();
    promptForConfirm();
    reload();
    return RETURN_CODE.SUCCESS.code();
  }

  private void promptForConfirm() {
    Scanner scanner = new Scanner(System.in);
    while (true) {
      LOG.info("After making sure target server is up, please enter y/Y to proceed regions reload: ");
      String y = scanner.nextLine();
      if (y.equalsIgnoreCase("y")) {
        return;
      }
    }
  }

  private ServerName findServer(String server) throws IOException {
    for (ServerName sn : admin.getClusterStatus().getServers()) {
      if (sn.getHostAndPort().equals(server)) {
        return sn;
      }
    }
    return null;
  }

  private void unload() throws Exception {
    move(findServer(temp_server.value()));
    LOG.info("Unload regions finished!");
  }

  private void reload() throws Exception {
    // Like it is restarted, startcode will get updated.
    move(findServer(target_server.value()));
    LOG.info("Reload regions finished!");
  }

  private void move(ServerName target) throws InterruptedException, BrokenBarrierException {
    for (HRegionInfo region : target_regions) {
      pool.submit(() -> {
        try {
          LOG.info("Moving " + region.getEncodedName() + " to " + target.getServerName());
          admin.move(region.getEncodedNameAsBytes(), Bytes.toBytes(target.getServerName()));
          LOG.info("Moved " + region.getEncodedName() + " to " + target.getServerName());
          barrier.await();
        } catch (IOException | InterruptedException | BrokenBarrierException e) {
          LOG.info("Error in moving " + region.getEncodedName());
        }
      });
    }
    barrier.await();
  }

  @Override protected void destroyToy() throws Exception {
    admin.close();
    super.destroyToy();
  }

}
