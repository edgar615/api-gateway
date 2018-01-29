package com.github.edgar615.direwolves.plugin.scope;

import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import com.github.edgar615.util.log.Log;
import com.github.edgar615.util.vertx.redis.RedisClientHelper;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;

/**
 * Created by Administrator on 2017/10/27.
 */
class ScopeLoader {
  private final Vertx vertx;

  private final JsonObject config;

  private final String url;

  private final int port;

  private final String prefix;

  private final RedisClient redisClient;

  ScopeLoader(Vertx vertx, String prefix, JsonObject config) {
    this.vertx = vertx;
    this.prefix = prefix;
    this.config = config;
    this.url = config.getString("url");
    this.port = config.getInteger("port", 80);
    this.redisClient = RedisClientHelper.getShared(vertx);
  }

  public void load(String userId, String apiScope, Handler<AsyncResult<String>> handler) {
    String cacheKey = "user:permission:" + userId;
    redisClient.hget(cacheKey, apiScope, ar -> {
      if (ar.succeeded() && ar.result() != null) {
        handler.handle(Future.succeededFuture(ar.result()));
        return;
      }
      httpLoad(userId, apiScope, loadResult -> {
        String pass = "0";
        if (loadResult.succeeded()) {
          pass = "1";
        }
        handler.handle(Future.succeededFuture(pass));
        //保存缓存
        redisClient.hset(cacheKey, apiScope, pass, Future.<Long>future().completer());
        //设置过期时间
        redisClient.expire(cacheKey, 30 * 60l, Future.<Long>future().completer());
      });
    });
  }

  public void httpLoad(String userId, String scope, Handler<AsyncResult<Void>> handler) {
    try {
      String path = url;
      if (path.contains("?")) {
        path = path + "&userId=" + userId + "&permission=" + scope;
      } else {
        path = path + "?userId=" + userId + "&permission=" + scope;
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
