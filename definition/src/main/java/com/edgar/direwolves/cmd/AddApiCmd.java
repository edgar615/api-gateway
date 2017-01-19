package com.edgar.direwolves.cmd;

import com.edgar.direwolves.core.cmd.ApiCmd;
import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.verticle.ApiDefinitionRegistry;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 新增或修改API，
 * 参数: api的JSON配置文件.
 * 命令字: api.add
 *
 * @author Edgar  Date 2017/1/19
 */
class AddApiCmd implements ApiCmd {

  @Override
  public String cmd() {
    return "api.add";
  }

  @Override
  public Future<JsonObject> doHandle(JsonObject jsonObject) {
    ApiDefinition apiDefinition = ApiDefinition.fromJson(jsonObject);
    if (apiDefinition != null) {
      ApiDefinitionRegistry.create().add(apiDefinition);
    }
    return Future.succeededFuture(succeedResult());
  }
}
