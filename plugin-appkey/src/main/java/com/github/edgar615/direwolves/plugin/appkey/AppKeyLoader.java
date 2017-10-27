package com.github.edgar615.direwolves.plugin.appkey;

import com.github.edgar615.util.vertx.cache.CacheLoader;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Administrator on 2017/10/27.
 */
class AppKeyLoader implements CacheLoader<String, JsonObject> {
  private final Vertx vertx;

  private final JsonObject config;

  private final Map<String, JsonObject> localAppKeys = new HashMap<>();

  private final String notExistsKey;

  AppKeyLoader(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    this.config = config;
    this.notExistsKey = config.getString("notExistsKey", UUID.randomUUID().toString());
    JsonArray jsonArray = config.getJsonArray("data", new JsonArray());
    for (int i = 0; i < jsonArray.size(); i++) {
      JsonObject jsonObject = config.getJsonArray("data").getJsonObject(i);
      String appKey = jsonObject.getString("appKey");
      if (appKey != null) {
        localAppKeys.put(appKey, jsonObject);
      }
    }
  }

  @Override
  public void load(String key, Handler<AsyncResult<JsonObject>> handler) {
    JsonObject notExists = new JsonObject().put(notExistsKey, notExistsKey);
    if (localAppKeys.containsKey(key)) {
      handler.handle(Future.succeededFuture(localAppKeys.get(key)));
      return;
    }
    if (config.getValue("loader") instanceof JsonObject) {
      JsonObject loaderConfig = config.getJsonObject("loader");
      String host = loaderConfig.getString("host", "localhost");
      int port = loaderConfig.getInteger("port", 80);
      String path = loaderConfig.getString("path", "/");
      vertx.createHttpClient().get(port, host, path, response -> {
        if (response.statusCode() >= 400) {
          //TODO LOG
          handler.handle(Future.succeededFuture(notExists));
        } else {
          response.bodyHandler(body -> {
            handler.handle(Future.succeededFuture(body.toJsonObject()));
          });
        }
      }).exceptionHandler(throwable -> throwable.printStackTrace())
              .end();
    } else {
      handler.handle(Future.succeededFuture(notExists));
    }


  }
}
