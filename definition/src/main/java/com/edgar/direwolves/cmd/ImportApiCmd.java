package com.edgar.direwolves.cmd;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.apidiscovery.ApiDiscoveryOptions;
import com.edgar.direwolves.core.apidiscovery.FileApiImporter;
import com.edgar.direwolves.core.cmd.ApiCmd;
import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.apidiscovery.ApiDiscovery;
import com.edgar.util.validation.Rule;
import com.edgar.util.validation.Validations;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

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
    if (config.containsKey("publishedAddress")) {
      configuration.put("publishedAddress", config.getString("publishedAddress"));
    }
    if (config.containsKey("unpublishedAddress")) {
      configuration.put("unpublishedAddress", config.getString("unpublishedAddress"));
    }
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
    ApiDiscovery discovery = ApiDiscovery.create(vertx, new ApiDiscoveryOptions().setName(namespace));
    discovery.registerServiceImporter(new FileApiImporter(), jsonObject, ar -> {
      if (ar.succeeded()) {
          future.complete(succeedResult());
      } else {
        future.fail(ar.cause());
      }
    });

    return future;
  }
}
