package com.github.edgar615.gateway.localcache;

import com.github.edgar615.gateway.core.cache.CacheFactory;
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

    @Override
    public Cache<String, JsonObject> create(Vertx vertx, String cacheName, CacheOptions options) {
        CacheManager cacheManager = CacheManager.instance();
        synchronized (LocalCacheFactory.class) {
            Cache<String, JsonObject> cache = cacheManager.getCache(cacheName);
            if (cache == null) {
                cache = new GuavaCache<>(vertx, cacheName, options);
                cacheManager.addCache(cache);
                return cache;
            }
            return cache;
        }
    }
}
