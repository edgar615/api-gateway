package com.github.edgar615.direwolves.core.cache;

import com.github.edgar615.util.vertx.cache.Cache;
import com.github.edgar615.util.vertx.cache.CacheOptions;
import com.github.edgar615.util.vertx.cache.GuavaCache;
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
  public Cache<String, JsonObject> create(Vertx vertx, String cacheName, CacheOptions options) {
    return new GuavaCache<>(vertx, cacheName, options);
  }
}
