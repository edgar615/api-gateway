package com.edgar.direwolves.cmd;

import com.edgar.direwolves.core.cmd.ApiCmd;
import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.ApiDiscovery;
import com.edgar.direwolves.verticle.ApiDefinitionRegistry;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.validation.Rule;
import com.edgar.util.validation.Validations;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.stream.Collectors;

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

  GetApiCmd(Vertx vertx) {this.vertx = vertx;
    rules.put("namespace", Rule.required());
    rules.put("name", Rule.required());
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
    ApiDiscovery discovery = ApiDiscovery.create(vertx, namespace);
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
