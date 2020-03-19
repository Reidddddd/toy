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

import org.apache.toy.common.BoolParameter;
import org.apache.toy.common.EnumParameter;
import org.apache.toy.common.Parameter;
import org.apache.toy.common.StringArrayParameter;

import java.util.List;

@SuppressWarnings("rawtypes")
public class GrantPhoenixAccess extends GrantAccessControl {

  private final Parameter<Enum> gv =
      EnumParameter.newBuilder("gpa.grant_revoke", G_V.G, G_V.class).setDescription("grant or revoke permission").setRequired().opt();
  private final Parameter<String[]> p_users =
      StringArrayParameter.newBuilder("gpa.users").setDescription("users who want to access phoenix").setRequired().opt();
  private final Parameter<Boolean> test_db = BoolParameter.newBuilder("gpa.acquire_test", false).setDescription("if grant permission for creating tables").opt();

  @Override protected void requisite(List<Parameter> requisites) {
    requisites.add(p_users);
    requisites.add(gv);
    requisites.add(test_db);
  }

  @Override protected void inCheck() {
    relation.setValue(RELATION.MULTI2MULTI);
    tables.setValue(new String[] { "SYSTEM:CATALOG", "SYSTEM:STATS" });
    permissions.setValue(test_db.value() ? new String[] { "RXW", "R" } : new String[] { "RX", "R" });
    users.setValue(p_users.value());
    g_v.setValue(gv.value());

    super.inCheck();
  }

}
