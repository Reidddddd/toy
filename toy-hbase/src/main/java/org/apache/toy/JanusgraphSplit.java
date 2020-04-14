package org.apache.toy;

import org.apache.hadoop.hbase.HRegionLocation;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.RegionLocator;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.toy.common.IntParameter;
import org.apache.toy.common.Parameter;
import org.apache.toy.common.StringParameter;

import java.nio.ByteBuffer;
import java.util.List;

public class JanusgraphSplit extends AbstractHBaseToy {

  private final Parameter<Integer> split_num =
      IntParameter.newBuilder("js.split_num").setRequired().setDescription("expect split numbers").opt();
  private final Parameter<String> table_name =
      StringParameter.newBuilder("js.table_name").setRequired().setDescription("table to be splited").opt();


  private Admin admin;

  @Override
  protected void requisite(List<Parameter> requisites) {
    requisites.add(split_num);
    requisites.add(table_name);
  }

  @Override
  protected void buildToy(ToyConfiguration configuration) throws Exception {
    super.buildToy(configuration);
    admin = connection.getAdmin();
  }

  @Override
  protected int haveFun() throws Exception {
    byte[][] split_keys = getSplitKeys(split_num.value());
    RegionLocator region_locator = connection.getRegionLocator(TableName.valueOf(table_name.value()));
    List<HRegionLocation> regions = region_locator.getAllRegionLocations();
    regions.sort((o1, o2) -> Bytes.compareTo(o1.getRegionInfo().getStartKey(), o2.getRegionInfo().getStartKey()));
    int i = 0;
    for (HRegionLocation region : regions) {
      System.out.println(
          Bytes.toHex(region.getRegionInfo().getStartKey()) + "|" + Bytes.toHex(region.getRegionInfo().getEndKey()) + "|"
              + Bytes.toHex(split_keys[i]) + "|"
              + Bytes.compareTo(region.getRegionInfo().getStartKey(), split_keys[i]) + "|"
              + Bytes.compareTo(split_keys[i], region.getRegionInfo().getEndKey())
      );
      i++;
    }
    return 0;
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
  }

  public static void main(String[] args) {
    int regionCount = 32;
    ByteBuffer startKey = ByteBuffer.allocate(4);
    ByteBuffer endKey = ByteBuffer.allocate(4);

    startKey.putInt((int)(((1L << 32) - 1L) / regionCount)).flip();
    endKey.putInt((int)(((1L << 32) - 1L) / regionCount * (regionCount - 1))).flip();
    byte[] start = startKey.array();
    byte[] end= endKey.array();
    System.out.println(start.length);
    System.out.println(end.length);
    byte[][] splits = Bytes.split(start, end, regionCount - 3);
    System.out.println(splits.length);
    for (int i = 0; i < splits.length; i++) {
      System.out.println(Bytes.toHex(splits[i]));
      i++;
    }
  }


}
