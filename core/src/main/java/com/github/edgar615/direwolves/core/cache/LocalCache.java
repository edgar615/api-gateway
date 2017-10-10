package com.github.edgar615.direwolves.core.cache;

import com.google.common.cache.CacheBuilder;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import java.util.concurrent.TimeUnit;

/**
 * Created by Edgar on 2017/8/30.
 *
 * @author Edgar  Date 2017/8/30
 */
public class LocalCache implements Cache {

  private final com.google.common.cache.Cache<String, JsonObject> cache;

  private final String name;

  public LocalCache(String name, CacheOptions options) {
    this.name = name;
    this.cache = CacheBuilder.newBuilder()
            .expireAfterWrite(options.getExpireAfterWrite(), TimeUnit.SECONDS)
            .build();
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public void get(String key, Handler<AsyncResult<JsonObject>> handler) {
    try {
      JsonObject jsonObject = cache.getIfPresent(key);
      handler.handle(Future.succeededFuture(jsonObject));
    } catch (Exception e) {
      handler.handle(Future.failedFuture(e));
    }
  }

  @Override
  public void put(String key, JsonObject value, Handler<AsyncResult<Void>> handler) {
    try {
      cache.put(key, value);
      handler.handle(Future.succeededFuture());
    } catch (Exception e) {
      handler.handle(Future.failedFuture(e));
    }
  }

  @Override
  public void evict(String key, Handler<AsyncResult<Void>> handler) {
    try {
      cache.invalidate(key);
      handler.handle(Future.succeededFuture());
    } catch (Exception e) {
      handler.handle(Future.failedFuture(e));
    }
  }
}
