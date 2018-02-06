package com.github.edgar615.direwolves.core.cache;

import com.github.edgar615.util.vertx.cache.Cache;
import com.github.edgar615.util.vertx.cache.CacheOptions;
import io.vertx.core.ServiceHelper;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 16-12-7.
 */
public interface CacheFactory {
  CacheFactory factory = ServiceHelper.loadFactory(CacheFactory.class);

  /**
   * @return 创建一个缓存
   */
  Cache<String, JsonObject> create(Vertx vertx, String cacheName, CacheOptions options);

}
