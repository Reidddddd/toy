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

import org.apache.aries.common.Constants;
import org.apache.aries.common.IntParameter;
import org.apache.aries.common.Parameter;
import org.apache.aries.common.StringParameter;
import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("rawtypes")
public class MergeTable extends AbstractHBaseToy {

  private final Parameter<String> merge_table_url =
      StringParameter.newBuilder("mt.url").setRequired().setDescription("The pages of the table. Please copy from HBase Web UI").opt();
  private final Parameter<Integer> merge_threshold =
      IntParameter.newBuilder("mt.merge_threshold_megabytes")
                  .setRequired().setDescription("Regions under this threshold will be merged, unit in MB").opt();

  @Override
  protected void requisite(List<Parameter> requisites) {
    requisites.add(merge_table_url);
    requisites.add(merge_threshold);
  }

  @Override
  protected void exampleConfiguration() {
    example(merge_table_url.key(), "http://host:port/table.jsp?name=namespace:table");
    example(merge_threshold.key(), "100");
  }

  Admin admin;
  TableName table;
  long threshold_bytes;

  @Override
  protected void buildToy(ToyConfiguration configuration) throws Exception {
    super.buildToy(configuration);
    admin = connection.getAdmin();
    int start = merge_table_url.value().indexOf("=") + 1;
    table = TableName.valueOf(merge_table_url.value().substring(start));
    threshold_bytes = merge_threshold.value() * Constants.ONE_MB;
  }

  @Override
  protected int haveFun() throws Exception {
    List<HRegionInfo> regions = admin.getTableRegions(table);

    Document doc = Jsoup.connect(merge_table_url.value()).get();
    Element element = doc.getElementById("regionServerDetailsTable");
    TableInfo table_info = new TableInfo(element);
    for (int i = 0, index_a, index_b; i < table_info.regionNum();) {
      index_a = i++;
      index_b = i++;
      if (index_b > table_info.regionNum()) {
        break;
      }
      RegionInfo region_A = table_info.getRegionAtIndex(index_a);
      RegionInfo region_B = table_info.getRegionAtIndex(index_b);
      if (region_A.getSizeInBytes() < threshold_bytes || region_B.getSizeInBytes() < threshold_bytes) {
        HRegionInfo A_region = regions.get(index_a);
        HRegionInfo B_region = regions.get(index_b);
        LOG.info("Merging region " + A_region.getRegionId() + " and " + B_region.getRegionId());
        admin.mergeRegions(
            A_region.getEncodedNameAsBytes(),
            B_region.getEncodedNameAsBytes(),
            false);
      }
    }
    return 0;
  }

  @Override
  protected void destroyToy() throws Exception {
    admin.close();
    super.destroyToy();
  }

  class RegionInfo {

    String name;
    String server;
    String read_requests;
    String write_requests;
    String file_size;
    String file_num;
    String mem_size;
    String locality;
    String start_key;
    String end_key;

    RegionInfo(Element region) {
      Elements column = region.select("td");
               int i = 0;
      name           = column.get(i++).text();
      server         = column.get(i++).text();
      read_requests  = column.get(i++).text();
      write_requests = column.get(i++).text();
      file_size      = column.get(i++).text();
      file_num       = column.get(i++).text();
      mem_size       = column.get(i++).text();
      locality       = column.get(i++).text();
      start_key      = column.get(i++).text();
      end_key        = column.get(i++).text();
    }

    public long getSizeInBytes() {
      long size;
      String num = file_size.split(" ")[0];
      if (file_size.contains("GB")) {
        float gb = Float.parseFloat(num); // get the number
        size = (long) (gb * Constants.ONE_GB);
      } else if (file_size.contains("MB")) {
        float mb = Float.parseFloat(num);
        size = (long) (mb * Constants.ONE_MB);
      } else if (file_size.contains("KB")) {
        float kb = Float.parseFloat(num);
        size = (long) (kb * Constants.ONE_KB);
      } else {
        size = Long.parseLong(num);
      }
      return size;
    }

    @Override
    public String toString() {
      return "RegionInfo{" +
          "name='" + name + '\'' +
          ", server='" + server + '\'' +
          ", read_requests='" + read_requests + '\'' +
          ", write_requests='" + write_requests + '\'' +
          ", file_size='" + file_size + '\'' +
          ", file_num='" + file_num + '\'' +
          ", mem_size='" + mem_size + '\'' +
          ", locality='" + locality + '\'' +
          ", start_key='" + start_key + '\'' +
          ", end_key='" + end_key + '\'' +
          '}';
    }

  }

  class TableInfo {

    final List<RegionInfo> regions;

    TableInfo(Element table) {
      Elements rows = table.select("tr");
      regions = new ArrayList<>(rows.size() - 1); // Skip first title row
      for (int i = 1; i < rows.size(); i++) {
        regions.add(new RegionInfo(rows.get(i)));
      }
    }

    public int regionNum() {
      return regions.size();
    }

    RegionInfo getRegionAtIndex(int i) {
      return regions.get(i);
    }

  }

}
