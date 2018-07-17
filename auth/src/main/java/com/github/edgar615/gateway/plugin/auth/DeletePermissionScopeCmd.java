package com.github.edgar615.gateway.plugin.auth;

import com.github.edgar615.gateway.core.cmd.ApiSubCmd;
import com.github.edgar615.gateway.core.definition.ApiDefinition;
import io.vertx.core.json.JsonObject;

/**
 * 删除鉴权的命令
 *命令字:plugin.permission.delete
 * @author Edgar  Date 2017/1/20
 */
public class DeletePermissionScopeCmd implements ApiSubCmd {

  public DeletePermissionScopeCmd() {
  }

  @Override
  public String cmd() {
    return "plugin.permission.delete";
  }

  @Override
  public void handle(ApiDefinition definition, JsonObject jsonObject) {
    definition.removePlugin(PermissionPlugin.class.getSimpleName());

  }

}
