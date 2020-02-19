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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.security.access.AccessControlClient;
import org.apache.hadoop.hbase.security.access.Permission;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.toy.common.Parameter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GrantRevokePermission extends AbstractHBaseToy {

  @SuppressWarnings("rawtypes")
  private final Parameter<Enum> grant_revoke =
      Parameter.<Enum>newBuilder()
               .setKey("grant_or_revoke").setType(G_V.class)
               .setRequired(true).setDescription("Grant or revoke action")
               .setDefaultValue(G_V.G)
               .opt();
  private final Parameter<String> user_name =
      Parameter.<String>newBuilder()
               .setKey("user_name").setType(String.class)
               .setRequired(true).setDescription("User to be granted permissions.")
               .opt();
  private final Parameter<String> table_name =
      Parameter.<String>newBuilder()
               .setKey("gv_table_name").setType(String.class)
               .setRequired(true).setDescription("Target table.")
               .opt();
  private final Parameter<String> permissions =
      Parameter.<String>newBuilder()
               .setKey("permissions").setType(String.class)
               .setRequired(true)
               .setDescription("Permissions are RWXCA, R for read, W for write, X for execute endpoint, C for create, A for admin.")
               .opt();
  private final Parameter<String[]> columns_permissions =
      Parameter.<String[]>newBuilder()
               .setKey("columns").setType(String[].class)
               .setDescription("Permissions for columns, delimited by ','. e.g. 'c1:q1,c2:q2' or 'c1,c2,c3'.")
               .opt();
  private final Parameter<Boolean> administrator =
      Parameter.<Boolean>newBuilder()
               .setKey("global").setType(Boolean.class)
               .setDescription("Grant global permissions.")
               .setDefaultValue(Boolean.FALSE)
               .opt();
  private final Parameter<String> namespace =
      Parameter.<String>newBuilder()
               .setKey("namespace").setType(String.class)
               .setDescription("Grant namespace permissions.")
               .opt();

  enum G_V {
    G, V
  }

  private Connection connection;
  private TableName table;
  private Permission.Action[] actions;
  private String[] columns;
  private G_V act;

  @Override
  protected void requisite(@SuppressWarnings("rawtypes") List<Parameter> requisites) {
    requisites.add(grant_revoke);
    requisites.add(user_name);
    requisites.add(table_name);
    requisites.add(permissions);
    requisites.add(columns_permissions);
    requisites.add(administrator);
    requisites.add(namespace);
  }

  @Override
  protected void buildToy(Configuration configuration) throws Exception {
    connection = ConnectionFactory.createConnection(configuration);
    table      = TableName.valueOf(table_name.value());
    actions    = extractPermissionActions(permissions.value());
    columns    = columns_permissions.empty() ? null : columns_permissions.value();
    act        = (G_V)grant_revoke.value();
  }

  private Permission.Action[] extractPermissionActions(String permission_actions) {
    Set<Permission.Action> actions = new HashSet<>();
    for (char c : permission_actions.toCharArray()) {
      switch (c) {
        case 'R': actions.add(Permission.Action.READ);    break;
        case 'W': actions.add(Permission.Action.WRITE);   break;
        case 'C': actions.add(Permission.Action.CREATE);  break;
        case 'X': actions.add(Permission.Action.EXEC);    break;
        case 'A': actions.add(Permission.Action.ADMIN);   break;
        default:                                          break;
      }
    }
    return actions.toArray(new Permission.Action[0]);
  }

  @Override
  protected int haveFun() throws Exception {
    if (!AccessControlClient.isAccessControllerRunning(connection)) return RETURN_CODE.FAILURE.code();

    try {
           if (!administrator.unset())          performGlobalPermissions(act);
      else if (!namespace.empty())              performNamespacePermissions(act);
      else if (!columns_permissions.empty())
               for (String column : columns)    performTableColumnPermissions(act, column);
      else                                      performTablePermissions(act);
    } catch (Throwable throwable) {
      throw new RuntimeException(throwable);
    }
    return RETURN_CODE.SUCCESS.code();
  }

  private void performGlobalPermissions(G_V act) throws Throwable {
    switch (act) {
      case G: AccessControlClient.grant(connection, user_name.value(), actions);  break;
      case V: AccessControlClient.revoke(connection, user_name.value(), actions); break;
      default:                                                                    break;
    }

  }

  private void performNamespacePermissions(G_V act) throws Throwable {
    switch (act) {
      case G: AccessControlClient.grant(connection, namespace.value(), user_name.value(), actions);   break;
      case V: AccessControlClient.revoke(connection, namespace.value(), user_name.value(), actions);  break;
      default:                                                                                        break;
    }

  }

  private void performTableColumnPermissions(G_V act, String column) throws Throwable {
    String[] family_qualifier = column.split(":");
    if (family_qualifier.length == 1) {
      switch (act) {
        case G: AccessControlClient.grant(connection, table, user_name.value(),
                                          Bytes.toBytes(family_qualifier[0]), null, actions);    break;
        case V: AccessControlClient.revoke(connection, table, user_name.value(),
                                          Bytes.toBytes(family_qualifier[0]), null, actions); break;
        default:                                                                                      break;
      }

    } else if (family_qualifier.length == 2) {
      switch (act) {
        case G: AccessControlClient.grant(connection, table, user_name.value(),
                                          Bytes.toBytes(family_qualifier[0]), Bytes.toBytes(family_qualifier[1]),
                                          actions); break;
        case V: AccessControlClient.revoke(connection, table, user_name.value(),
                                          Bytes.toBytes(family_qualifier[0]), Bytes.toBytes(family_qualifier[1]),
                                          actions); break;
        default:                                    break;
      }
    } else {
      throw new Throwable("Column " + column + " has problem.");
    }
  }

  private void performTablePermissions(G_V act) throws Throwable {
    switch (act) {
      case G: AccessControlClient.grant(connection, table, user_name.value(), null, null, actions);     break;
      case V: AccessControlClient.revoke(connection, table, user_name.value(), null, null, actions); break;
      default:                                                                                                      break;
    }
  }

  @Override
  protected void destroyToy() throws Exception {
    connection.close();
  }

}
