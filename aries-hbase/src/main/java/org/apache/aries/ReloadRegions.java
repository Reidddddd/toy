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
import org.apache.aries.common.StringArrayParameter;
import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.protobuf.ProtobufUtil;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ReloadRegions extends AbstractHBaseToy {

  private final Parameter<String[]> target_servers =
      StringArrayParameter.newBuilder("rr.target_servers").setRequired()
          .setDescription("Regions on these server will be unloaded, then reloaded. Please use server:port format, delimited by ','").opt();
  private final Parameter<String[]> temp_servers =
      StringArrayParameter.newBuilder("rr.temp_servers").setRequired()
          .setDescription("These servers will be used for store the regions from target servers temporarily.").opt();
  private final Parameter<Integer> thread_pool_size =
      IntParameter.newBuilder("rr.threads_for_move_regions").setDefaultValue(8).setDescription("Number of threads for moving regions.").opt();

  @Override protected String getParameterPrefix() {
    return "rr";
  }

  @Override protected void requisite(List<Parameter> requisites) {
    requisites.add(target_servers);
    requisites.add(temp_servers);
    requisites.add(thread_pool_size);
  }

  @Override protected void exampleConfiguration() {
    example(target_servers.key(), "target_server_1.com:5678,target_server_2.com:5678");
    example(temp_servers.key(), "temp_server_1.com:5678,temp_server_2.com:5678");
    example(thread_pool_size.key(), "8");
  }

  Admin admin;
  List<HRegionInfo> target_regions;
  ExecutorService pool;

  ServerName target;
  ServerName temp;

  @Override protected void buildToy(ToyConfiguration configuration) throws Exception {
    super.buildToy(configuration);
    admin = connection.getAdmin();
    pool = Executors.newFixedThreadPool(thread_pool_size.value());
  }

  @Override
  protected void midCheck() {
    if (target_servers.value().length != temp_servers.value().length) {
      throw new IllegalArgumentException("Target servers size should be equal to temp servers size");
    }
  }

  @Override protected int haveFun() throws Exception {
    int size = target_servers.value().length;
    for (int i = 0; i < size; i++) {
      target = findServer(target_servers.value()[i]);
      temp = findServer(temp_servers.value()[i]);
      target_regions = ProtobufUtil.getOnlineRegions(HConnectionManager.getConnection(connection.getConfiguration()).getAdmin(target));
      LOG.info("There are " + target_regions.size() + " regions on " + target);
      unloadRegionsTo(temp);
      promptForConfirm();
      reloadRegionsTo(target);
    }
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

  private void unloadRegionsTo(ServerName server) throws Exception {
    move(server);
    LOG.info("Unload regions finished!");
  }

  private void reloadRegionsTo(ServerName server) throws Exception {
    // Like it is restarted, startcode will get updated.
    move(server);
    LOG.info("Reload regions finished!");
  }

  private void move(ServerName target) {
    AtomicInteger moved = new AtomicInteger(target_regions.size());
    for (HRegionInfo region : target_regions) {
      pool.submit(() -> {
        try {
          LOG.info("Moving " + region.getEncodedName() + " to " + target.getServerName());
          admin.move(region.getEncodedNameAsBytes(), Bytes.toBytes(target.getServerName()));
          LOG.info("Moved " + region.getEncodedName() + " to " + target.getServerName());
          moved.decrementAndGet();
        } catch (IOException e) {
          LOG.info("Error in moving " + region.getEncodedName());
        }
      });
    }
    while (moved.get() != 0);
  }

  @Override protected void destroyToy() throws Exception {
    admin.close();
    super.destroyToy();
  }

}
