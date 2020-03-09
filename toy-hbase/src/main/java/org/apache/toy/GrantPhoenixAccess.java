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

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.security.access.AccessControlClient;
import org.apache.toy.common.Parameter;
import org.apache.toy.common.StringArrayParameter;

import java.util.List;
import java.util.stream.IntStream;

public class GrantPhoenixAccess extends GrantRevokePermission {

  private final TableName CATALOG = TableName.valueOf("SYSTEM:CATALOG");
  private final TableName   STATS = TableName.valueOf("SYSTEM:STATS");

  private final Parameter<String[]> users =
      StringArrayParameter.newBuilder("gpa.users").setDescription("users who want to access phoenix").setRequired().opt();

  @Override protected void requisite(List<Parameter> requisites) {
    requisites.add(users);
  }

  @Override protected void preCheck(ToyConfiguration configuration, List<Parameter> requisites) {
    skipCheck = true;
    super.preCheck(configuration, requisites);
  }

  @Override protected int haveFun() throws Exception {
    if (!AccessControlClient.isAccessControllerRunning(connection)) return RETURN_CODE.FAILURE.code();

    IntStream.range(0, users.value().length)
             .forEach(i -> {
               try {
                 performTablePermission(G_V.G, CATALOG, users.value()[i], extractPermissionActions("RX"));
                 performTablePermission(G_V.G,   STATS, users.value()[i], extractPermissionActions("R"));
               } catch (Throwable throwable) {
                 throw new RuntimeException(throwable);
               }
             });

    return RETURN_CODE.SUCCESS.code();
  }

}
