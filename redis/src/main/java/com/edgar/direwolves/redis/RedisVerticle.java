package com.edgar.direwolves.redis;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;

/**
 * Created by Edgar on 2017/9/5.
 *
 * @author Edgar  Date 2017/9/5
 */
public class RedisVerticle extends AbstractVerticle {

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    RedisClient redisClient = RedisClient.create(vertx, new RedisOptions(config()));
    super.start(startFuture);
  }
}
