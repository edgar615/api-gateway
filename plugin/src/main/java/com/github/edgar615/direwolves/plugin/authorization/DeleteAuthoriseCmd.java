package com.github.edgar615.direwolves.plugin.authorization;

import com.github.edgar615.direwolves.core.cmd.ApiSubCmd;
import com.github.edgar615.direwolves.core.definition.ApiDefinition;
import io.vertx.core.json.JsonObject;

/**
 * 删除鉴权的命令
 *命令字:authorise.delete
 * @author Edgar  Date 2017/1/20
 */
public class DeleteAuthoriseCmd implements ApiSubCmd {

  public DeleteAuthoriseCmd() {
  }

  @Override
  public String cmd() {
    return "authorise.delete";
  }

  @Override
  public void handle(ApiDefinition definition, JsonObject jsonObject) {
    definition.removePlugin(AuthorisePlugin.class.getSimpleName());

  }

}
