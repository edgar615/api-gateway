package com.edgar.direwolves.redis;

import com.google.common.base.Strings;

import com.edgar.direwolves.core.cache.Cache;
import com.edgar.direwolves.core.cache.CacheFactory;
import com.edgar.direwolves.core.cache.CacheOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;

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
    JsonObject redisConfig = config.getJsonObject("redis", new JsonObject());
//    String redisHost = redisConfig.getString("host", "localhost");
//    int redisPort = redisConfig.getInteger("port", 6379);
//    String redisAuth = redisConfig.getString("auth", "");
//    RedisOptions options = new RedisOptions(redisConfig);
//    if (!Strings.isNullOrEmpty(redisAuth)) {
//      options.setAuth(redisAuth);
//    }
    RedisClient redisClient = RedisClient.create(vertx, new RedisOptions(redisConfig));
    return new RedisCache(redisClient, cacheName, new CacheOptions(config));
  }
}
