package com.github.edgar615.direwolves.core.cache;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/8/30.
 *
 * @author Edgar  Date 2017/8/30
 */
public class LocalCacheFactory implements CacheFactory {

  private static final String TYPE = "local";

  @Override
  public String type() {
    return TYPE;
  }

  @Override
  public Cache create(Vertx vertx, String cacheName, JsonObject config) {
    return new LocalCache(cacheName, new CacheOptions(config));
  }
}
