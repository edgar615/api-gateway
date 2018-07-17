package com.github.edgar615.gateway.cmd;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.github.edgar615.gateway.core.apidiscovery.ApiDiscovery;
import com.github.edgar615.gateway.core.apidiscovery.ApiDiscoveryOptions;
import com.github.edgar615.gateway.core.cmd.ApiCmd;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import com.github.edgar615.util.validation.Rule;
import com.github.edgar615.util.validation.Validations;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

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

  private final Vertx vertx;

  private final JsonObject configuration = new JsonObject();

  GetApiCmd(Vertx vertx, JsonObject config) {this.vertx = vertx;
    rules.put("namespace", Rule.required());
    rules.put("name", Rule.required());
    setConfig(config, configuration);
  }

  @Override
  public String cmd() {
    return "api.get";
  }

  @Override
  public Future<JsonObject> doHandle(JsonObject jsonObject) {
    Validations.validate(jsonObject.getMap(), rules);
    Validations.validate(jsonObject.getMap(), rules);
    String namespace = jsonObject.getString("namespace");
    String name = jsonObject.getString("name", "*");
    JsonObject filter = new JsonObject();
    if (name != null) {
      filter.put("name", name);
    }
    Future<JsonObject> future = Future.future();
    ApiDiscovery discovery = ApiDiscovery.create(vertx, new ApiDiscoveryOptions().setName(namespace));
    discovery.getDefinitions(filter, ar -> {
      if (ar.failed()) {
        future.fail(ar.cause());
        return;
      }
      if (ar.result().isEmpty()) {
        future.fail(SystemException.create(DefaultErrorCode
                                                   .RESOURCE_NOT_FOUND)
                            .set("name", name));
      } else {
        future.complete(ar.result().get(0).toJson());
      }
    });
    return future;

  }
}
