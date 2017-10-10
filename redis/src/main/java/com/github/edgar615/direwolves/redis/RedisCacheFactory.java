package com.github.edgar615.direwolves.redis;

import com.github.edgar615.direwolves.core.cache.Cache;
import com.github.edgar615.direwolves.core.cache.CacheFactory;
import com.github.edgar615.direwolves.core.cache.CacheOptions;
import com.github.edgar615.util.vertx.redis.RedisClientHelper;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;

/**
 * Created by edgar on 16-12-9.
 */
public class RedisCacheFactory implements CacheFactory {

  private static final String TYPE = "redis";

  @Override
  public String type() {
    return TYPE;
  }

  @Override
  public Cache create(Vertx vertx, String cacheName, JsonObject config) {
    //RedisVerticle 要先部署
    RedisClient redisClient = RedisClientHelper.getShared(vertx);
    return new RedisCache(redisClient, cacheName, new CacheOptions(config));
  }
}
