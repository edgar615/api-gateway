package com.edgar.direwolves.redis;

import com.edgar.direwolves.core.cache.Cache;
import com.edgar.direwolves.core.cache.CacheOptions;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;

/**
 * Created by edgar on 16-12-9.
 */
public class RedisCache implements Cache {

  private final RedisClient redisClient;

  /**
   * 过期时间
   */
  private long expires;

  private String name;

  RedisCache(RedisClient redisClient, String name, CacheOptions options) {
    this.redisClient = redisClient;
    this.expires = options.getExpireAfterWrite();
    this.name = name;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public void get(String key, Handler<AsyncResult<JsonObject>> handler) {
    redisClient.hgetall(key, handler);
  }

  @Override
  public void put(String key, JsonObject value, Handler<AsyncResult<Void>> handler) {
    Future<String> future = Future.future();
    redisClient.hmset(key, value, future.completer());

    future.compose(s -> {
      Future<Long> exFuture = Future.future();
      redisClient.expire(key, expires, exFuture.completer());
      return exFuture;
    }).setHandler(ar -> {
      if (ar.succeeded()) {
        handler.handle(Future.succeededFuture());
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  @Override
  public void evict(String key, Handler<AsyncResult<Void>> handler) {
    redisClient.del(key, ar -> {
      if (ar.succeeded()) {
        handler.handle(Future.succeededFuture());
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

}
