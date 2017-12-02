package com.github.edgar615.direvolves.plugin.authentication;

import com.github.edgar615.util.vertx.cache.CacheLoader;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.UUID;

/**
 * Created by Administrator on 2017/10/27.
 */
class UserLoader implements CacheLoader<String, JsonObject> {
  private final Vertx vertx;

  private final JsonObject config;

  private final String notExistsKey;

  private final String prefix;

  UserLoader(Vertx vertx, String prefix, JsonObject config) {
    this.vertx = vertx;
    this.prefix = prefix;
    this.config = config;
    this.notExistsKey = config.getString("notExistsKey", UUID.randomUUID().toString());
  }

  @Override
  public void load(String key, Handler<AsyncResult<JsonObject>> handler) {
    String userId = key.substring(prefix.length());
    JsonObject notExists = new JsonObject().put(notExistsKey, notExistsKey);
    httpLoader(userId, handler, notExists);
  }

  private void httpLoader(String userId, Handler<AsyncResult<JsonObject>> handler, JsonObject
          notExists) {
    try {
      int port = config.getInteger("port", 80);
      String path = config.getString("url", "/");
      if (path.contains("?")) {
        path = path + "&userId=" + userId;
      } else {
        path = path + "?userId=" + userId;
      }
      vertx.createHttpClient().get(port, "127.0.0.1", path, response -> {
        if (response.statusCode() >= 400) {
          handler.handle(Future.succeededFuture(notExists));
        } else {
          response.bodyHandler(body -> {
            handler.handle(Future.succeededFuture(body.toJsonObject()));
          });
        }
      }).exceptionHandler(e -> handler.handle(Future.succeededFuture(notExists)))
              .end();
    } catch (Exception e) {
      handler.handle(Future.succeededFuture(notExists));
    }
  }
}