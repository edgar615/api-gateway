package com.edgar.direwolves.cmd;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.apidiscovery.ApiDiscoveryOptions;
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

  public ImportApiCmd(Vertx vertx) {
    this.vertx = vertx;
    rules.put("namespace", Rule.required());
    rules.put("path", Rule.required());
  }

  @Override
  public String cmd() {
    return "api.import";
  }

  @Override
  public Future<JsonObject> doHandle(JsonObject jsonObject) {
    Validations.validate(jsonObject.getMap(), rules);
    String namespace = jsonObject.getString("namespace");
    String path = jsonObject.getString("path");

    ApiDiscovery discovery = ApiDiscovery.create(vertx, new ApiDiscoveryOptions().setName(namespace));

    Future<JsonObject> future = Future.future();

    vertx.<List<String>>executeBlocking(f -> {
      try {
        List<String> apiList = readFromFile(path);
        f.complete(apiList);
      } catch (Exception e) {
        f.fail(e);
      }
    }, ar -> {
      if (ar.succeeded()) {
        List<Future> futures = addApiList(discovery, ar.result());
        checkResult(namespace, futures, future);
      } else {
        LOGGER.error("---| [Import Api] [FAILED] [{}]", ar.cause().getMessage());
        future.fail(ar.cause());
      }
    });

    return future;
  }

  private void checkResult(String namespace, List<Future> futures, Future<JsonObject> complete) {
    CompositeFuture.all(futures)
            .setHandler(ar -> {
              if (ar.succeeded()) {
                complete.complete(new JsonObject().put("result", futures.size())
                                          .put("namespace", namespace));
                return;
              }
              complete.fail(ar.cause());
            });
  }

  private List<Future> addApiList(ApiDiscovery discovery, List<String> apiList) {
    List<Future> futures = new ArrayList<Future>();
    for (String str : apiList) {
      try {
        ApiDefinition d = ApiDefinition.fromJson(new JsonObject(str));
        Future<ApiDefinition> addFuture = addApi(discovery, d);
        futures.add(addFuture);
      } catch (Exception e) {
        LOGGER.error("[api.imported] [{}]",str,  e);
      }
    }
    return futures;
  }

  private Future<ApiDefinition> addApi(ApiDiscovery discovery, ApiDefinition definition) {
    Future<ApiDefinition> future = Future.future();
    discovery.publish(definition, ar -> {
      if (ar.failed()) {
        future.fail(ar.cause());
        return;
      }
      future.complete(ar.result());
    });
    return future;
  }

  private List<String> readFromFile(String path) {
    List<String> datas = new ArrayList<>();
    if (Files.isDirectory(new File(path).toPath())) {
      List<String> paths = vertx.fileSystem().readDirBlocking(path);
      for (String p : paths) {
        if (Files.isDirectory(new File(p).toPath())) {
          datas.addAll(readFromFile(p));
        } else {
          String defineJson = vertx.fileSystem().readFileBlocking(p).toString();
          datas.add(defineJson);
        }
      }
    } else {
      Buffer buffer = vertx.fileSystem().readFileBlocking(path);
      try {
        String defineJson = buffer.toString();
        datas.add(defineJson);
      } catch (Exception e) {
        LOGGER.error("[file.readed] [FAILED] [{}]", path
                                                      + ":" + e.getMessage());
      }
    }
    return datas;
  }
}
