package com.edgar.direwolves.core.cache;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/9/5.
 *
 * @author Edgar  Date 2017/9/5
 */
public class CacheAdapter implements Cache {
  /**
   * 过期时间
   */
  private long expires;

  private String name;

  @Override
  public String name() {
    return null;
  }

  @Override
  public void get(String key, Handler<AsyncResult<JsonObject>> handler) {

  }

  @Override
  public void put(String key, JsonObject value, Handler<AsyncResult<Void>> handler) {

  }

  @Override
  public void evict(String key, Handler<AsyncResult<Void>> handler) {

  }
}
