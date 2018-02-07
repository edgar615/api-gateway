package com.github.edgar615.direvolves.plugin.auth;

import com.github.edgar615.direwolves.core.cmd.ApiSubCmd;
import com.github.edgar615.direwolves.core.definition.ApiDefinition;
import io.vertx.core.json.JsonObject;

/**
 * 删除鉴权的命令
 *命令字:plugin.scope.delete
 * @author Edgar  Date 2017/1/20
 */
public class DeleteScopeCmd implements ApiSubCmd {

  public DeleteScopeCmd() {
  }

  @Override
  public String cmd() {
    return "plugin.scope.delete";
  }

  @Override
  public void handle(ApiDefinition definition, JsonObject jsonObject) {
    definition.removePlugin(ScopePlugin.class.getSimpleName());

  }

}
