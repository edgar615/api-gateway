package com.edgar.direwolves.redis;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.Shareable;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;

/**
 * 用于RedisClient的共享，代码参考了MySQL / PostgreSQL client中
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
class ClientHolder implements Shareable {

  private final Vertx vertx;

  private final JsonObject config;

  private final Runnable closeRunner;

  private RedisClient client;

  private int refCount = 1;

  ClientHolder(Vertx vertx, JsonObject config, Runnable closeRunner) {
    this.vertx = vertx;
    this.config = config;
    this.closeRunner = closeRunner;
  }

  synchronized RedisClient client() {
    if (client == null) {
      client = RedisClient.create(vertx, new RedisOptions(config));
    }
    return client;
  }

  synchronized void incRefCount() {
    refCount++;
  }

  synchronized void close(Handler<AsyncResult<Void>> whenDone) {
    if (--refCount == 0) {
      if (client != null) {
        client.close(whenDone);
      }
      if (closeRunner != null) {
        closeRunner.run();
      }
    }
  }
}