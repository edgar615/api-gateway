package com.github.edgar615.gateway.redis;

import com.github.edgar615.util.log.Log;
import com.github.edgar615.util.vertx.redis.RedisClientHelper;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Edgar on 2017/9/5.
 *
 * @author Edgar  Date 2017/9/5
 */
public class RedisVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(RedisVerticle.class);

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    Log.create(LOGGER)
            .setEvent("redis.deploying")
            .addData("config", config())
            .info();
    JsonObject redisConfig = config().getJsonObject("redis", new JsonObject());
    RedisClient redisClient = RedisClientHelper.createShared(vertx, redisConfig);
    redisClient.ping(ar -> {
      if (ar.succeeded()) {
        Log.create(LOGGER)
                .setEvent("redis.started.succeed")
                .info();
        startFuture.complete();
      } else {
        Log.create(LOGGER)
                .setEvent("redis.started.failed")
                .error();
        startFuture.fail(ar.cause());
      }
    });
  }
}
