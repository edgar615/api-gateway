package com.github.edgar615.gateway.redis;

import com.github.edgar615.gateway.core.cache.CacheFactory;
import com.github.edgar615.util.vertx.cache.Cache;
import com.github.edgar615.util.vertx.cache.CacheOptions;
import com.github.edgar615.util.vertx.redis.RedisClientHelper;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;

/**
 * Created by edgar on 16-12-9.
 */
public class RedisCacheFactory implements CacheFactory {

    @Override
    public Cache<String, JsonObject> create(Vertx vertx, String cacheName, CacheOptions options) {
        //RedisVerticle 要先部署
        RedisClient redisClient = RedisClientHelper.getShared(vertx);
        return new RedisCache(redisClient, cacheName, options);
    }
}
