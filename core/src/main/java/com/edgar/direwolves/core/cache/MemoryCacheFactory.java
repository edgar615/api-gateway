package com.edgar.direwolves.core.cache;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 16-12-7.
 */
public class MemoryCacheFactory implements CacheFactory {
  private static final String TYPE = "memory";

  @Override
  public String type() {
    return TYPE;
  }

  @Override
  public CacheProvider create(Vertx vertx, JsonObject config) {
    return new MemoryCacheProvider(config);
  }

}
