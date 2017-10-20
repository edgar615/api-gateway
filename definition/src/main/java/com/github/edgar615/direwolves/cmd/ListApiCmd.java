package com.github.edgar615.direwolves.cmd;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.github.edgar615.direwolves.core.apidiscovery.ApiDiscoveryOptions;
import com.github.edgar615.direwolves.core.cmd.ApiCmd;
import com.github.edgar615.direwolves.core.definition.ApiDefinition;
import com.github.edgar615.direwolves.core.apidiscovery.ApiDiscovery;
import com.github.edgar615.util.validation.Rule;
import com.github.edgar615.util.validation.Validations;
import com.github.edgar615.util.vertx.JsonUtils;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
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
  private final Multimap<String, Rule> rules = ArrayListMultimap.create();

  private final Vertx vertx;

  private final JsonObject configuration = new JsonObject();

  ListApiCmd(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    rules.put("namespace", Rule.required());
    setConfig(config, configuration);
  }

  @Override
  public String cmd() {
    return "api.list";
  }

  @Override
  public Future<JsonObject> doHandle(JsonObject jsonObject) {
    Validations.validate(jsonObject.getMap(), rules);
    String namespace = jsonObject.getString("namespace");
    Integer start = JsonUtils.getInteger(jsonObject, "start", 0);
    Integer limit = JsonUtils.getInteger(jsonObject, "limit", 10);
    String name = jsonObject.getString("name", "*");
    JsonObject filter = new JsonObject();
    if (name != null) {
      filter.put("name", name);
    }
    Future<JsonObject> future = Future.future();
    ApiDiscovery.create(vertx, new ApiDiscoveryOptions().setName(namespace + ".api"))
            .getDefinitions(filter, ar -> {
              if (ar.failed()) {
                future.fail(ar.cause());
                return;
              }
              List<ApiDefinition> definitions = ar.result();
              if (start > definitions.size()) {
                future.complete(new JsonObject()
                                        .put("result", new JsonArray()));
                return;
              }
              int toIndex = start + limit;
              if (toIndex > definitions.size()) {
                toIndex = definitions.size();
              }
              List<JsonObject> result = definitions
                      .stream()
                      .sorted((o1, o2) -> o1.name().compareToIgnoreCase(o2.name()))
                      .collect(Collectors.toList())
                      .subList(start, toIndex).stream()
                      .map(d -> d.toJson())
                      .collect(Collectors.toList());

              future.complete(new JsonObject()
                                      .put("result", result));
            });
    return future;
  }
}
