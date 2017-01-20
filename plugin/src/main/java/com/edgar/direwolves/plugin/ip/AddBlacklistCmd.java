package com.edgar.direwolves.plugin.ip;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.cmd.ApiCmd;
import com.edgar.direwolves.core.definition.ApiProvider;
import com.edgar.util.validation.Rule;
import com.edgar.util.validation.Validations;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ProxyHelper;

/**
 * Created by Edgar on 2017/1/20.
 *
 * @author Edgar  Date 2017/1/20
 */
public class AddBlacklistCmd implements ApiCmd {

  private final Vertx vertx;

  private final ApiProvider apiProvider;
  private final Multimap<String, Rule> rules = ArrayListMultimap.create();

  AddBlacklistCmd(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    String address = config.getString("api.provider.address", "direwolves.api.provider");
    this.apiProvider = ProxyHelper.createProxy(ApiProvider.class, vertx, address);
    rules.put("name", Rule.required());
    rules.put("ip", Rule.required());
  }

  @Override
  public String cmd() {
    return "api.blacklist.add";
  }

  @Override
  public Future<JsonObject> doHandle(JsonObject jsonObject) {
    String name = jsonObject.getString("name");
    Validations.validate(jsonObject.getMap(), rules);
    String ip = jsonObject.getString("ip");
    Future<JsonObject> future = Future.future();
    apiProvider.list(name, ar -> {
      if (ar.succeeded()) {
        future.complete(new JsonObject()
                                .put("result", ar.result()));
      } else {
        future.fail(ar.cause());
      }
    });
    return future;
  }
}
