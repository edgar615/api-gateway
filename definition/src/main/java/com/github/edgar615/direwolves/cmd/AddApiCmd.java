package com.github.edgar615.direwolves.cmd;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.github.edgar615.direwolves.core.apidiscovery.ApiDiscovery;
import com.github.edgar615.direwolves.core.apidiscovery.ApiDiscoveryOptions;
import com.github.edgar615.direwolves.core.cmd.ApiCmd;
import com.github.edgar615.direwolves.core.definition.ApiDefinition;
import com.github.edgar615.util.validation.Rule;
import com.github.edgar615.util.validation.Validations;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * 新增或修改API，
 * 参数: api的JSON配置文件.
 * 命令字: api.add
 *
 * @author Edgar  Date 2017/1/19
 */
class AddApiCmd implements ApiCmd {
  private final Multimap<String, Rule> rules = ArrayListMultimap.create();

  private final Vertx vertx;

  private final JsonObject configuration = new JsonObject();

  AddApiCmd(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    rules.put("namespace", Rule.required());
    rules.put("data", Rule.required());
    setConfig(config, configuration);
  }

  @Override
  public String cmd() {
    return "api.add";
  }

  @Override
  public Future<JsonObject> doHandle(JsonObject jsonObject) {
//    Log.create(LOGGER)
//            .setTraceId(jsonObject.getString("traceId"))
//            .setModule("api.cmd")
//            .setEvent(cmd())
//            .addData("data", jsonObject.encode())
//            .info();

    Validations.validate(jsonObject.getMap(), rules);
    String namespace = jsonObject.getString("namespace");
    String data = jsonObject.getString("data");
    ApiDefinition apiDefinition = ApiDefinition.fromJson(new JsonObject(data));
    Future<JsonObject> future = Future.future();
    ApiDiscovery.create(vertx, new ApiDiscoveryOptions(configuration).setName(namespace + ".api"))
            .publish(apiDefinition, ar -> {
              if (ar.failed()) {
                future.fail(ar.cause());
                return;
              }
              future.complete(succeedResult());
            });
    return future;
  }
}
