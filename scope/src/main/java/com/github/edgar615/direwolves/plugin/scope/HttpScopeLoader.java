package com.github.edgar615.direwolves.plugin.scope;

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
class HttpScopeLoader {
  private final Vertx vertx;

  private final JsonObject config;

  private final String url;

  private final int port;

  HttpScopeLoader(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    this.config = config;
    this.url = config.getString("url");
    this.port = config.getInteger("port", 80);
  }

  public void load(String identifier, String scope, Handler<AsyncResult<Void>> handler) {
    try {
      String path = url;
      if (path.contains("?")) {
        path = path + "&identifier=" + identifier + "&permission=" + scope;
      } else {
        path = path + "?identifier=" + identifier + "&permission=" + scope;
      }
      vertx.createHttpClient().get(port, "127.0.0.1", path, response -> {
        if (response.statusCode() >= 400) {
          handler.handle(Future.failedFuture("no permission"));
        } else {
          handler.handle(Future.succeededFuture());
        }
      }).exceptionHandler(e -> handler.handle(Future.failedFuture("no permission")))
              .end();
    } catch (Exception e) {
      handler.handle(Future.failedFuture("no permission"));
    }
  }

}
