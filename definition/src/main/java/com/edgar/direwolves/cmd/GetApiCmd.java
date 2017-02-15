package com.edgar.direwolves.cmd;

import com.edgar.direwolves.core.cmd.ApiCmd;
import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.verticle.ApiDefinitionRegistry;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.validation.Rule;
import com.edgar.util.validation.Validations;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * 根据名称获取某个API定义，
 * 参数: api的JSON配置文件.
 * 命令字: api.get
 * 参数 {name : 要删除的API名称}
 *
 * @author Edgar  Date 2017/1/19
 */
class GetApiCmd implements ApiCmd {

  private final Multimap<String, Rule> rules = ArrayListMultimap.create();

  GetApiCmd() {
    rules.put("name", Rule.required());
  }

  @Override
  public String cmd() {
    return "api.get";
  }

  @Override
  public Future<JsonObject> doHandle(JsonObject jsonObject) {
    Validations.validate(jsonObject.getMap(), rules);
    String name = jsonObject.getString("name");
    List<ApiDefinition> definitions = ApiDefinitionRegistry.create().filter(name);
    if (definitions.isEmpty()) {
      throw SystemException.create(DefaultErrorCode.RESOURCE_NOT_FOUND)
              .set("details", "Api->" + name);
    }
    return Future.succeededFuture(new JsonObject()
        .put("result", definitions.get(0).toJson()));
  }
}
