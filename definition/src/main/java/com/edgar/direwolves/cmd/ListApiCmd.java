package com.edgar.direwolves.cmd;

import com.edgar.direwolves.core.cmd.ApiCmd;
import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.verticle.ApiDefinitionRegistry;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 查询api列表，
 * 命令字: api.list
 * 参数 {name : API名称，默认值null;
 * start : 开始索引，默认值0;
 * list : 取多少条记录，默认值10;}
 *
 * @author Edgar  Date 2017/1/19
 */
class ListApiCmd implements ApiCmd {

  @Override
  public String cmd() {
    return "api.list";
  }

  @Override
  public Future<JsonObject> doHandle(JsonObject jsonObject) {
    Integer start = jsonObject.getInteger("start", 0);
    Integer limit = jsonObject.getInteger("limit", 10);
    String name = jsonObject.getString("name", null);
    List<ApiDefinition> definitions = ApiDefinitionRegistry.create().filter(name);
    int toIndex = start + limit;
    if (toIndex > definitions.size()) {
      toIndex = definitions.size();
    }
    List<JsonObject> result = definitions.subList(start, toIndex).stream()
        .map(d -> d.toJson())
        .collect(Collectors.toList());
    return Future.succeededFuture(new JsonObject()
        .put("result", result));
  }
}
