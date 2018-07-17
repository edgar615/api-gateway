package com.github.edgar615.gateway.localcache;

import com.github.edgar615.util.vertx.cache.Cache;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/8/30.
 *
 * @author Edgar  Date 2017/8/30
 */
public interface CacheManager {
  Cache<String, JsonObject> getCache(String cacheName);

  Cache<String, JsonObject> addCache(Cache<String, JsonObject> cache);

  static CacheManager instance() {
    return CacheManagerImpl.instance();
  }
}
