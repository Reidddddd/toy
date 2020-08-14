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
import org.apache.hadoop.hbase.client.Admin;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;

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

  @Override
  protected void buildToy(ToyConfiguration configuration) throws Exception {
    super.buildToy(configuration);
    admin = connection.getAdmin();
  }

  @Override
  protected int haveFun() throws Exception {
    Document doc = Jsoup.connect(merge_table_url.value()).get();
    Element element = doc.selectFirst("tbody");
    for (Element ele : element.getAllElements()) {
      System.out.println(ele);
    }
    return 0;
  }

  @Override
  protected void destroyToy() throws Exception {
    admin.close();
    super.destroyToy();
  }
}
