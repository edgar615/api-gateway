package com.edgar.direwolves.redis;

import com.google.common.base.Strings;

import com.edgar.direwolves.core.cache.CacheFactory;
import com.edgar.direwolves.core.cache.RedisProvider;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;

/**
 * Created by edgar on 16-12-9.
 */
public class RedisProviderFactory implements CacheFactory {

  @Override
  public RedisProvider create(Vertx vertx, JsonObject config) {
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
    return new RedisProviderImpl(vertx, redisClient, Future.future());
  }
}
