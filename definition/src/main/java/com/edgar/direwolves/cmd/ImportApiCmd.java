package com.edgar.direwolves.cmd;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.cmd.ApiCmd;
import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.verticle.ApiDefinitionRegistry;
import com.edgar.util.validation.Rule;
import com.edgar.util.validation.Validations;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
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
    rules.put("path", Rule.required());
  }

  @Override
  public String cmd() {
    return "api.import";
  }

  @Override
  public Future<JsonObject> doHandle(JsonObject jsonObject) {
    Validations.validate(jsonObject.getMap(), rules);
    String path = jsonObject.getString("path");

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
        JsonArray succeed = new JsonArray();
        List<String> apiList = ar.result();
        for (String str : apiList) {
          try {
            JsonObject json = new JsonObject(str);
            ApiDefinition d = ApiDefinition.fromJson(json);
            ApiDefinitionRegistry.create().add(d);
            LOGGER.info("---| [Import Api] [OK] [{}]", d.name());
            succeed.add(d.name());
          } catch (Exception e) {
            LOGGER.error("---| [Import Api] [FAILED] [{}]", e.getMessage());
          }
        }
        future.complete(new JsonObject().put("total", apiList.size())
                                .put("succeed", succeed.size()));
      } else {
        LOGGER.error("---| [Import Api] [FAILED] [{}]", ar.cause().getMessage());
        future.fail(ar.cause());
      }
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
        LOGGER.error("---| [Read Api] [FAILED] [{}]", path
                                                      + ":" + e.getMessage());
      }
    }
    return datas;
  }
}
