package com.github.edgar615.direwolves.cmd;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.github.edgar615.direwolves.core.apidiscovery.ApiDiscoveryOptions;
import com.github.edgar615.direwolves.definition.FileApiImporter;
import com.github.edgar615.direwolves.core.cmd.ApiCmd;
import com.github.edgar615.direwolves.core.apidiscovery.ApiDiscovery;
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
public class ImportApiCmd implements ApiCmd {

  private final Multimap<String, Rule> rules = ArrayListMultimap.create();

  private final Vertx vertx;

  private final JsonObject configuration = new JsonObject();

  public ImportApiCmd(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    rules.put("namespace", Rule.required());
    rules.put("path", Rule.required());
    setConfig(config, configuration);
  }

  @Override
  public String cmd() {
    return "api.import";
  }

  @Override
  public Future<JsonObject> doHandle(JsonObject jsonObject) {
    Validations.validate(jsonObject.getMap(), rules);
    String namespace = jsonObject.getString("namespace");
    Future<JsonObject> future = Future.future();
    ApiDiscovery discovery = ApiDiscovery.create(vertx, new ApiDiscoveryOptions().setName(namespace + ".api"));
    discovery.registerImporter(new FileApiImporter(), jsonObject, ar -> {
      if (ar.succeeded()) {
          future.complete(succeedResult());
      } else {
        future.fail(ar.cause());
      }
    });

    return future;
  }
}
