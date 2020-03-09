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
import org.apache.hadoop.hbase.security.access.Permission;
import org.apache.toy.common.EnumParameter;
import org.apache.toy.common.Parameter;
import org.apache.toy.common.StringArrayParameter;
import org.apache.toy.common.ToyUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

public class GrantRevokePermission extends AbstractHBaseToy {

  private final Parameter<String[]> grant_revoke =
      StringArrayParameter.newBuilder("gv.grant_or_revoke").setRequired().setDescription("Grant or revoke action").opt();
  @SuppressWarnings("rawtypes")
  private final Parameter<Enum> scope =
      EnumParameter.newBuilder("gv.scope", SCOPE.TABLE, SCOPE.class).setRequired().setDescription("Permission scope").opt();
  private final Parameter<String[]> user_name =
      StringArrayParameter.newBuilder("gv.user_name").setRequired().setDescription("Users to be granted/revoked permissions").opt();
  private final Parameter<String[]> permissions =
      StringArrayParameter.newBuilder("gv.permissions").setRequired()
                          .setDescription("Permissions are RWXCA, R for read, W for write, X for execute endpoint, C for create, A for admin").opt();
  private final Parameter<String[]> namespace =
      StringArrayParameter.newBuilder("gv.namespace").setDescription("Namespace permissions").opt();
  private final Parameter<String[]> table_name =
      StringArrayParameter.newBuilder("gv.table_name").setDescription("Target table").opt();

  enum G_V {
    G, V
  }

  enum SCOPE {
    GLOBAL, NAMESPACE, TABLE, FAMILY, QUALIFIER
  }

  private final Permission.Action[] def_actions =
      new Permission.Action[] {
          Permission.Action.READ,
          Permission.Action.WRITE,
          Permission.Action.ADMIN,
          Permission.Action.EXEC,
          Permission.Action.CREATE
      };
  private SCOPE p_scope;

  @Override
  protected void requisite(@SuppressWarnings("rawtypes") List<Parameter> requisites) {
    requisites.add(grant_revoke);
    requisites.add(scope);
    requisites.add(user_name);
    requisites.add(permissions);
    requisites.add(namespace);
    requisites.add(table_name);
  }

  @Override
  protected void buildToy(ToyConfiguration configuration) throws Exception {
    super.buildToy(configuration);
          p_scope    = (SCOPE)scope.value();
    int valid_length = user_name.value().length;
    switch (p_scope) {
      case    GLOBAL:                                                               break;
      case NAMESPACE: ToyUtils.assertLengthValid(namespace.value(), valid_length);  break;
      case     TABLE: ToyUtils.assertLengthValid(table_name.value(), valid_length); break;
             default: valid_length = 0;                                             break;
    }
            ToyUtils.assertLengthValid(permissions.value(), valid_length);
            ToyUtils.assertLengthValid(grant_revoke.value(), valid_length);
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

    switch (p_scope) {
      case GLOBAL:
        IntStream.range(0, user_name.value().length)
                 .forEach(i -> {
                       try {
                         performGlobalPermissions(
                             G_V.valueOf(grant_revoke.value()[i]),
                             user_name.value()[i],
                             extractPermissionActions(permissions.value()[i])
                             );
                       } catch (Throwable throwable) {
                         throw new RuntimeException(throwable);
                       }
                     }
                 ); break;
      case NAMESPACE:
        IntStream.range(0, user_name.value().length)
                 .forEach(i -> {
                       try {
                         performNamespacePermissions(
                             G_V.valueOf(grant_revoke.value()[i]),
                             namespace.value()[i],
                             user_name.value()[i],
                             extractPermissionActions(permissions.value()[i])
                             );
                       } catch (Throwable throwable) {
                         throw new RuntimeException(throwable);
                       }
                     }
                 ); break;
      case TABLE:
        IntStream.range(0, user_name.value().length)
                 .forEach(i -> {
                       try {
                         performTablePermission(
                             G_V.valueOf(grant_revoke.value()[i]),
                             TableName.valueOf(table_name.value()[i]),
                             user_name.value()[i],
                             extractPermissionActions(permissions.value()[i])
                             );
                       } catch (Throwable throwable) {
                         throw new RuntimeException(throwable);
                       }
                     }
                 ); break;
    }
    return RETURN_CODE.SUCCESS.code();
  }

  private void performGlobalPermissions(G_V act, String user_name, Permission.Action[] actions) throws Throwable {
    switch (act) {
      case  G: AccessControlClient.grant(connection, user_name, actions);      break;
      case  V: AccessControlClient.revoke(connection, user_name, def_actions); break;
      default:                                                                 break;
    }
  }

  private void performNamespacePermissions(G_V act, String namespace, String user_name, Permission.Action[] actions) throws Throwable {
    switch (act) {
      case  G: AccessControlClient.grant(connection, namespace, user_name, actions);      break;
      case  V: AccessControlClient.revoke(connection, namespace, user_name, def_actions); break;
      default:                                                                            break;
    }
  }

  private void performTablePermission(G_V act, TableName table, String user_name, Permission.Action[] actions) throws Throwable {
    switch (act) {
      case  G: AccessControlClient.grant(connection, table, user_name, null, null, actions);         break;
      case  V: AccessControlClient.revoke(connection, table, user_name, null, null, def_actions); break;
      default:                                                                                                   break;
    }
  }

}
