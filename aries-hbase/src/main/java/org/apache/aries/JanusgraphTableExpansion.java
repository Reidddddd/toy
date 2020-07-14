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

import org.apache.hadoop.hbase.HRegionLocation;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.RegionLocator;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.aries.common.IntParameter;
import org.apache.aries.common.Parameter;
import org.apache.aries.common.StringParameter;

import java.nio.ByteBuffer;
import java.util.List;

public class JanusgraphTableExpansion extends AbstractHBaseToy {

  private final Parameter<Integer> old_split_num =
      IntParameter.newBuilder("js.old_split_num").setRequired().setDescription("old split numbers").opt();
  private final Parameter<Integer> new_split_num =
      IntParameter.newBuilder("js.new_split_num").setRequired().setDescription("new split numbers").opt();
  private final Parameter<String> table_name =
      StringParameter.newBuilder("js.table_name").setRequired().setDescription("table to be splited").opt();

  private Admin admin;

  @Override
  protected void requisite(List<Parameter> requisites) {
    requisites.add(old_split_num);
    requisites.add(new_split_num);
    requisites.add(table_name);
  }

  @Override
  protected void buildToy(ToyConfiguration configuration) throws Exception {
    super.buildToy(configuration);

    if (new_split_num.value() / old_split_num.value() != 2) {
      throw new IllegalArgumentException(new_split_num.key() + " should be twice of " + old_split_num.key());
    }
  }

  @Override
  protected int haveFun() throws Exception {
    byte[][] split_keys = getSplitKeys(new_split_num.value());
    RegionLocator region_locator = connection.getRegionLocator(TableName.valueOf(table_name.value()));
    List<HRegionLocation> regions = region_locator.getAllRegionLocations();
    regions.sort((o1, o2) -> Bytes.compareTo(o1.getRegionInfo().getStartKey(), o2.getRegionInfo().getStartKey()));
    for (int i = 0; i < regions.size(); i++) {
      HRegionLocation region = regions.get(i);
      if (!verifyKeyRange(region.getRegionInfo().getStartKey(), region.getRegionInfo().getEndKey(), split_keys[i])) {
        System.out.println(Bytes.toHex(split_keys[i]) + " is not in range [" +
            Bytes.toHex(region.getRegionInfo().getStartKey()) + ", " +
            Bytes.toHex(region.getRegionInfo().getEndKey()));
        continue;
      }
      System.out.println("Splitting region " + region.getRegionInfo().getRegionNameAsString() + " with key " + Bytes.toHex(split_keys[i]));
      admin.splitRegion(region.getRegionInfo().getRegionName(), split_keys[i]);
    }
    return 0;
  }

  private boolean verifyKeyRange(byte[] start_key, byte[] end_key, byte[] split_key) {
    if (start_key.length == 0) {
      return Bytes.compareTo(split_key, end_key) < 0;
    }
    if (end_key.length == 0) {
      return Bytes.compareTo(start_key, split_key) < 0;
    }
    return Bytes.compareTo(start_key, split_key) < 0 && Bytes.compareTo(split_key, end_key) < 0;
  }

  private byte[][] getSplitKeys(int region_count) {
    byte[][] all_keys = Bytes.split(getStartKey(region_count), getEndKey(region_count), region_count - 3);
    byte[][] split_keys = new byte[all_keys.length / 2 + 1][];
    for (int i = 0, j = 0; j < all_keys.length; i += 1, j += 2) {
      split_keys[i] = all_keys[j];
    }
    return split_keys;
  }

  private byte[] getStartKey(int region_count) {
    ByteBuffer start_key = ByteBuffer.allocate(4);
    start_key.putInt((int)(((1L << 32) - 1L) / region_count)).flip();
    return start_key.array();
  }

  private byte[] getEndKey(int region_count) {
    ByteBuffer end_key = ByteBuffer.allocate(4);
    end_key.putInt((int)(((1L << 32) - 1L) / region_count * (region_count - 1))).flip();
    return end_key.array();
  }

  @Override
  protected void destroyToy() throws Exception {
    super.destroyToy();
    admin.close();;
  }

}
