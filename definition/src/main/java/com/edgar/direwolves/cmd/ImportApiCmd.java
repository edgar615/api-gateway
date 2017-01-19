package com.edgar.direwolves.cmd;

import com.edgar.direwolves.core.cmd.ApiCmd;
import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.verticle.ApiDefinitionRegistry;
import com.edgar.util.validation.Rule;
import com.edgar.util.validation.Validations;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 新增或修改API，
 * 参数: api的JSON配置文件.
 * 命令字: api.add
 *
 * @author Edgar  Date 2017/1/19
 */
class ImportApiCmd implements ApiCmd {

  private final Multimap<String, Rule> rules = ArrayListMultimap.create();

  private final Vertx vertx;

  ImportApiCmd(Vertx vertx) {
    this.vertx = vertx;
    rules.put("path", Rule.required());
  }

  @Override
  public String cmd() {
    return "api.import";
  }

  private List<JsonObject> readFromFile(String path) {
    List<JsonObject> datas = new ArrayList<>();
    if (Files.isDirectory(new File(path).toPath())) {
      List<String> paths = vertx.fileSystem().readDirBlocking(path);
      for (String p : paths) {
        if (Files.isDirectory(new File(p).toPath())) {
          datas.addAll(readFromFile(p));
        } else {
          JsonObject defineJson = vertx.fileSystem().readFileBlocking(p).toJsonObject();
          datas.add(defineJson);
        }
      }
    } else {
      Buffer buffer = vertx.fileSystem().readFileBlocking(path);
      System.out.println(buffer.toString());
      JsonObject defineJson = buffer.toJsonObject();
      datas.add(defineJson);
    }
    return datas;
  }

  @Override
  public Future<JsonObject> doHandle(JsonObject jsonObject) {
    Validations.validate(jsonObject.getMap(), rules);
    String path = jsonObject.getString("path");
    List<ApiDefinition> definitions = readFromFile(path)
        .stream().map(json -> ApiDefinition.fromJson(json))
        .collect(Collectors.toList());
    for (ApiDefinition definition : definitions) {
      ApiDefinitionRegistry.create().add(definition);
    }
    return Future.succeededFuture(succeedResult());
  }
}
