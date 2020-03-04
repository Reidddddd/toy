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

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.toy.common.Parameter;
import org.apache.toy.common.StringArrayParameter;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TopicDeletion extends AbstractKafkaToy {

  private final Parameter<String[]> topics =
      StringArrayParameter.newBuilder("td.delete_topics")
                          .setRequired().setDescription("Topics to be deleted.")
                          .addConstraint(v -> v.length >= 1)
                          .opt();

  private AdminClient ac;

  @Override
  protected void requisite(List<Parameter> requisites) {
    requisites.add(topics);
  }

  @Override
  protected void buildToy(ToyConfiguration configuration) throws Exception {
    ac = AdminClient.create(configuration.getProperties());
  }

  @Override
  protected int haveFun() throws Exception {
    ac.deleteTopics(Arrays.asList(topics.value()));
    return RETURN_CODE.SUCCESS.code();
  }

  @Override
  protected void destroyToy() throws Exception {
    ac.close(5, TimeUnit.SECONDS);
  }

}
