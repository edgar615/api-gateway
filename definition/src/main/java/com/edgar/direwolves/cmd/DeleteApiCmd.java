package com.edgar.direwolves.cmd;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.cmd.ApiCmd;
import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.ApiDiscovery;
import com.edgar.direwolves.verticle.ApiDefinitionRegistry;
import com.edgar.util.validation.Rule;
import com.edgar.util.validation.Validations;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 删除API.
 * 命令字: api.delete
 * <p>
 * 参数 {name : 要删除的API名称}
 * 如果name=null，会查找所有的权限映射.
 * name支持两种通配符 user*会查询所有以user开头的name，如user.add．
 * *user会查询所有以user结尾对name,如add_user.
 * *表示所有.**也表示所有.但是***表示中间有一个*的字符串,如user*add
 *
 * @author Edgar  Date 2017/1/19
 */
class DeleteApiCmd implements ApiCmd {

  private final Vertx vertx;

  private final Multimap<String, Rule> rules = ArrayListMultimap.create();

  DeleteApiCmd(Vertx vertx) {
    this.vertx = vertx;
    rules.put("namespace", Rule.required());
    rules.put("name", Rule.required());
  }

  @Override
  public String cmd() {
    return "api.delete";
  }

  @Override
  public Future<JsonObject> doHandle(JsonObject jsonObject) {
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
              List<String> names = ar.result()
                      .stream()
                      .map(d -> d.name())
                      .collect(Collectors.toList());
              if (names.isEmpty()) {
                future.complete(succeedResult());
              } else {
                deleteByName(discovery, names, future);
              }
            });
    return future;
  }

  private void deleteByName(ApiDiscovery discovery, List<String> names,
                            Future<JsonObject>          complete) {
    List<Future> futures = new ArrayList<>();
    for (String name : names) {
      Future<Void> future = Future.future();
      futures.add(future);
      discovery.unpublish(name, ar -> {
        if (ar.succeeded()) {
          future.complete();
        } else {
          future.fail(ar.cause());
        }
      });
    }
    CompositeFuture.all(futures)
            .setHandler(ar -> {
              if (ar.succeeded()) {
                complete.complete(succeedResult());
              } else {
                complete.fail(ar.cause());
              }
            });
  }
}
