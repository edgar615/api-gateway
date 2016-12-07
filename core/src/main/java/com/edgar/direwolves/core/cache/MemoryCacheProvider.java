package com.edgar.direwolves.core.cache;

import com.edgar.util.cache.Cache;
import com.edgar.util.cache.LRUCache;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import java.util.NoSuchElementException;

/**
 * Created by edgar on 16-12-7.
 */
public class MemoryCacheProvider implements CacheProvider {

  private Cache<String, JsonObject> cache;

  public MemoryCacheProvider(JsonObject config) {
    String name = config.getString("cache.name", "DirewolvesCache");
    int maxSize = config.getInteger("cache.size", Integer.MAX_VALUE);
    float loadFactor = config.getFloat("cache.factor", 0.75f);
    this.cache = LRUCache.builder()
        .setLoadFactor(loadFactor)
        .setMaxSize(maxSize)
        .setName(name)
        .build();
  }

  @Override
  public void get(String key, Handler<AsyncResult<JsonObject>> handler) {
    JsonObject value = cache.get(key);
    if (value == null) {
      handler.handle(Future.failedFuture(new NoSuchElementException(key)));
    } else {
      handler.handle(Future.succeededFuture(value));
    }
  }

  @Override
  public void set(String key, JsonObject value, Handler<AsyncResult<JsonObject>> handler) {
    cache.put(key, value);
    handler.handle(Future.succeededFuture(new JsonObject()
        .put("result", 0)));
  }

  @Override
  public void setex(String key, JsonObject value, long expires, Handler<AsyncResult<JsonObject>> handler) {
    cache.put(key, value, expires);
    handler.handle(Future.succeededFuture(new JsonObject()
        .put("result", 0)));
  }

  @Override
  public void delete(String key, Handler<AsyncResult<JsonObject>> handler) {
    cache.delete(key);
    handler.handle(Future.succeededFuture(new JsonObject()
        .put("result", 0)));
  }
}
