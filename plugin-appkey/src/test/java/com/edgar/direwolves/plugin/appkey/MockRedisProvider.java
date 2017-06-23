package com.edgar.direwolves.plugin.appkey;

import com.edgar.direwolves.core.cache.RedisProvider;
import com.edgar.util.cache.Cache;
import com.edgar.util.cache.ExpiringCache;
import com.edgar.util.cache.LRUCache;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Created by edgar on 16-12-11.
 */
public class MockRedisProvider implements RedisProvider {

  private Cache<String, JsonObject> cache = new ExpiringCache<>(
          LRUCache.<String, JsonObject>builder().build());


  @Override
  public void get(String key, Handler<AsyncResult<JsonObject>> handler) {
    JsonObject value = cache.get(key);
    if (value == null) {
      handler.handle(Future.failedFuture(new NoSuchElementException()));
    } else {
      handler.handle(Future.succeededFuture(value));
    }
  }

  @Override
  public void set(String key, JsonObject value, Handler<AsyncResult<Void>> handler) {
    cache.put(key, value);
    handler.handle(Future.succeededFuture());
  }

  @Override
  public void setex(String key, JsonObject value, int expires, Handler<AsyncResult<Void>> handler) {
    cache.put(key, value, expires);
    handler.handle(Future.succeededFuture());
  }

  @Override
  public void delete(String key, Handler<AsyncResult<Void>> handler) {
    cache.delete(key);
    handler.handle(Future.succeededFuture());
  }

  @Override
  public void scriptLoad(String script, Handler<AsyncResult<String>> handler) {

  }

  @Override
  public void evalsha(String sha1, List<String> keys, List<String> args,
                      Handler<AsyncResult<JsonArray>> handler) {

  }

  @Override
  public void eval(String script, List<String> keys, List<String> args,
                   Handler<AsyncResult<JsonArray>> handler) {

  }

  @Override
  public void acquireToken(JsonArray rules, Handler<AsyncResult<JsonObject>> handler) {

  }
}
