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
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableExistsException;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.io.compress.Compression;
import org.apache.hadoop.hbase.io.encoding.DataBlockEncoding;
import org.apache.hadoop.hbase.regionserver.BloomType;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.aries.common.BoolParameter;
import org.apache.aries.common.Constants;
import org.apache.aries.common.EnumParameter;
import org.apache.aries.common.IntParameter;
import org.apache.aries.common.Parameter;
import org.apache.aries.common.StringArrayParameter;
import org.apache.aries.common.StringParameter;

import java.util.List;

/**
 * A class for creating table.
 */
@SuppressWarnings("rawtypes")
public class CreateTable extends AbstractHBaseToy {

  private final Parameter<String> table_name =
      StringParameter.newBuilder("ct.table_name").setRequired().setDescription("table name").opt();
  private final Parameter<String[]> families =
      StringArrayParameter.newBuilder("ct.families_name").setRequired().setDescription("A family or families delimited by ','")
                          .addConstraint(v -> v.length >= 1).opt();
  private final Parameter<Enum> split_algorithm =
      EnumParameter.newBuilder("ct.split_algorithm", ALGORITHM.NONE, ALGORITHM.class)
                   .setRequired().setDescription("split algorithm, so far suport HEX and NUM").opt();
  private final Parameter<Integer> hex_split_regions =
      IntParameter.newBuilder("ct.hex_split_regions").setDescription("Number of regions expecting when using hex split algorithm, upper bound is 256")
                  .addConstraint(v -> v > 1).addConstraint(v -> v <= 256).addConstraint(v -> 256 % v == 0).opt();
  private final Parameter<Integer> dec_split_regions =
      IntParameter.newBuilder("ct.dec_split_regions").setDescription("Number of regions expecting when using number split algorithm, upper bound is 1000")
                  .addConstraint(v -> v > 1).addConstraint(v -> v <= 1000).addConstraint(v -> 1000 % v == 0).opt();
  private final Parameter<String> table_owners =
      StringParameter.newBuilder("ct.table_owners").setRequired().setDescription("Whom the table is under in charge by, delimited by ','").opt();
  // Column family parameters
  // Following are optionals
  private final Parameter<Enum> compression =
      EnumParameter.newBuilder("ct.compression", Compression.Algorithm.NONE, Compression.Algorithm.class)
                   .setDescription("Compression algorithm will be used in flush and compactions").opt();
  private final Parameter<Boolean> cache_data_on_write =
      BoolParameter.newBuilder("ct.cache_data_on_write", false).setDescription("Cache data even when write, it is useful if your access pattern is read recently").opt();
  private final Parameter<Boolean> cache_data_in_L1 =
      BoolParameter.newBuilder("ct.cache_data_in_L1", false).setDescription("L1 is the fastest cache, but with the smallest size").opt();
  private final Parameter<Integer> time_to_live =
      IntParameter.newBuilder("ct.time_to_live").setDefaultValue(HConstants.FOREVER).setDescription("Time to live for cells under a specific family")
                  .addConstraint(v -> v > 0).opt();
  private final Parameter<Enum> bloom_type =
      EnumParameter.newBuilder("ct.bloom_filter_type", BloomType.NONE, BloomType.class)
                   .setDescription("Bloom filter is useful for row get or column get. set it ROW or ROWCOL").opt();
  private final Parameter<Integer> max_versions =
      IntParameter.newBuilder("ct.versions").setDefaultValue(1).setDescription("Min versions of a cell, 1 by default").opt();
  private final Parameter<Enum> data_block_encoding =
      EnumParameter.newBuilder("ct.data_block_encoding", DataBlockEncoding.NONE, DataBlockEncoding.class)
                   .setDescription("Encoding method for data block. Supporting type: PREFIX, DIFF, FAST_DIFF, ROW_INDEX_V1").opt();
  private final Parameter<Boolean> in_memory =
      BoolParameter.newBuilder("ct.in_memory", false).setDescription("data cached in memory region of block cache").opt();

  private TableName table;
  private Admin admin;
  private SplitAlgorithm split;

  @Override
  protected void requisite(List<Parameter> requisites) {
    requisites.add(table_name);
    requisites.add(families);
    requisites.add(split_algorithm);
    requisites.add(hex_split_regions);
    requisites.add(dec_split_regions);
    requisites.add(table_owners);
  }

  @Override
  protected void buildToy(ToyConfiguration configuration) throws Exception {
    super.buildToy(configuration);
    table = TableName.valueOf(table_name.value());
    admin = connection.getAdmin();
    split = buildSplitAlgorithm(split_algorithm.value());
  }

  @Override
  public int haveFun() throws Exception {
    if (admin.tableExists(table)) {
      throw new TableExistsException(table);
    }

    HTableDescriptor descriptor = buildTableDescriptor();
    for (String f : families.value()) {
      descriptor.addFamily(buildFamilyDescriptor(f, connection.getConfiguration()));
    }
    admin.createTable(descriptor, split.getSplitsKeys());
    return RETURN_CODE.SUCCESS.code();
  }

  private HTableDescriptor buildTableDescriptor() {
    HTableDescriptor descriptor = new HTableDescriptor(table);
    descriptor.setValue(Bytes.toBytes("TABLE_OWNERS"), Bytes.toBytes(table_owners.value()));
    return descriptor;
  }

  private HColumnDescriptor buildFamilyDescriptor(String family, Configuration conf) {
    extractFamilyConf(family, conf);

    HColumnDescriptor
           descriptor = new HColumnDescriptor(family);
           descriptor.setBlocksize(Constants.ONE_KB * 32); // 32KB
           descriptor.setCompressionType((Compression.Algorithm)compression.value());
           descriptor.setCacheDataOnWrite(cache_data_on_write.value());
           descriptor.setCacheDataInL1(cache_data_in_L1.value());
           descriptor.setTimeToLive(time_to_live.value());
           descriptor.setBloomFilterType((BloomType)bloom_type.value());
           descriptor.setMaxVersions(max_versions.value());
           descriptor.setDataBlockEncoding((DataBlockEncoding)data_block_encoding.value());
           descriptor.setInMemory(in_memory.value());
    return descriptor;
  }

  private void extractFamilyConf(String f, Configuration conf) {
      String suffix = "." + f;
            compression.setValue(conf.getEnum(compression.key() + suffix, compression.defvalue()));
    cache_data_on_write.setValue(conf.getBoolean(cache_data_on_write.key() + suffix, cache_data_on_write.defvalue()));
       cache_data_in_L1.setValue(conf.getBoolean(cache_data_in_L1.key() + suffix, cache_data_in_L1.defvalue()));
           time_to_live.setValue(conf.getInt(time_to_live.key() + suffix, time_to_live.defvalue()));
             bloom_type.setValue(conf.getEnum(bloom_type.key() + suffix, bloom_type.defvalue()));
           max_versions.setValue(conf.getInt(max_versions.key() + suffix, max_versions.defvalue()));
    data_block_encoding.setValue(conf.getEnum(data_block_encoding.key() + suffix, data_block_encoding.defvalue()));
              in_memory.setValue(conf.getBoolean(in_memory.key() + suffix, in_memory.defvalue()));
  }

  private SplitAlgorithm buildSplitAlgorithm(Enum raw_algorithm) {
    ALGORITHM algorithm = (ALGORITHM) raw_algorithm;
    switch (algorithm) {
      case HEX: return new HexSplitAlgorithm(hex_split_regions.value());
      case DEC: return new DecSplitAlgorithm(dec_split_regions.value());
      default:  return new NoneSplitAlgorithm();
    }
  }

  enum ALGORITHM {
    NONE, HEX, DEC
  }

  @Override
  protected void destroyToy() throws Exception {
    admin.close();
    super.destroyToy();
  }

  private interface SplitAlgorithm {

    byte[][] getSplitsKeys();

  }

  private class NoneSplitAlgorithm implements SplitAlgorithm {

    @Override public byte[][] getSplitsKeys() {
      return null;
    }

  }

  private abstract class NumericSplitAlgorithm implements SplitAlgorithm {

    byte[][] split_key;
    int interval;
    int pad_size;

    NumericSplitAlgorithm(int max_splits, int expect_splits, int radix) {
      // To split x regions, we only need x-1 split keys
      split_key = new byte[expect_splits - 1][];
      interval = max_splits / expect_splits;
      // For removing trailing zero, e.g. expect_splits=10
      // the interval will be 1000 / 10 = 100;
      // but the split key should be [1,2,3,4,5,6,7,8,9] instead of [100,200,300,400,500,...,900]
      if (radix == 10) {
        pad_size = interval % 100 == 0 ? 1 : interval % 10 == 0 ? 2 : 3;
        interval = interval % 100 == 0 ? interval / 100 :
                   interval % 10 == 0  ? interval / 10 :
                   interval;
      } else if (radix == 16) {
        pad_size = interval % 256 == 0 ? 2 : interval % 16 == 0 ? 1 : 2;
        interval = interval % 256 == 0 ? interval / 256 :
                   interval % 16 == 0  ? interval / 16 :
                   interval;
      }
    }

    @Override public byte[][] getSplitsKeys() {
      for (int i = interval, j = 0; j < split_key.length; i += interval, j++) {
        split_key[j] = Bytes.toBytes(paddingWithZero(pad_size, covert(i)));
      }
      return split_key;
    }

    abstract String covert(int key);

    private String paddingWithZero(int size, String to_be_padded) {
      if (size == to_be_padded.length()) return to_be_padded;

      StringBuilder leadingZero = new StringBuilder();
      int pad = size - to_be_padded.length();
      while (pad-- > 0) leadingZero.append("0");
      return leadingZero.append(to_be_padded).toString();
    }

  }

  private class DecSplitAlgorithm extends NumericSplitAlgorithm {

    DecSplitAlgorithm(int expect_splits) {
      super(1000, expect_splits, 10);
    }

    @Override String covert(int key) {
      return String.valueOf(key);
    }

  }

  private class HexSplitAlgorithm extends NumericSplitAlgorithm {

    HexSplitAlgorithm(int expect_splits) {
      super(256, expect_splits, 16);
    }

    @Override String covert(int key) {
      return Integer.toHexString(key);
    }

  }

}
