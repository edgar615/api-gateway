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
    String redisHost = config.getString("redis.host", "localhost");
    int redisPort = config.getInteger("redis.port", 6379);
    String redisAuth = config.getString("redis.password", "");

    RedisOptions options = new RedisOptions()
            .setHost(redisHost)
            .setPort(redisPort);
    if (!Strings.isNullOrEmpty(redisAuth)) {
      options.setAuth(redisAuth);
    }
    String namespace = config.getString("namespace", "");
    options.setAddress(namespace + options.getAddress());
    RedisClient redisClient = RedisClient.create(vertx, options);
    return new RedisCache(redisClient, cacheName, new CacheOptions(config));
  }
}
