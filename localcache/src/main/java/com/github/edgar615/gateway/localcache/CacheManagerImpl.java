package com.github.edgar615.gateway.localcache;

import com.github.edgar615.util.vertx.cache.Cache;
import io.vertx.core.json.JsonObject;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Edgar on 2017/8/30.
 *
 * @author Edgar  Date 2017/8/30
 */
class CacheManagerImpl implements CacheManager {
    private static final CacheManager INSTANCE = new CacheManagerImpl();

    private final ConcurrentMap<String, Cache<String, JsonObject>> cacheMap =
            new ConcurrentHashMap();

    private CacheManagerImpl() {
    }

    static CacheManager instance() {
        return INSTANCE;
    }

    @Override
    public Cache<String, JsonObject> getCache(String cacheName) {
        return cacheMap.get(cacheName);
    }

    @Override
    public Cache<String, JsonObject> addCache(Cache<String, JsonObject> cache) {
        return cacheMap.put(cache.name(), cache);
    }

}
