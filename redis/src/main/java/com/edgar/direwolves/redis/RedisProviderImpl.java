package com.edgar.direwolves.redis;

import com.edgar.direwolves.core.cache.RedisProvider;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by edgar on 16-12-9.
 */
public class RedisProviderImpl implements RedisProvider {
  private static final Logger LOGGER = LoggerFactory.getLogger(RedisProvider.class);

  private final RedisClient redisClient;

  /**
   * 脚本
   */
  private String tokenBucketScript;

  RedisProviderImpl(Vertx vertx, RedisClient redisClient, Future<Void> completed) {
    this.redisClient = redisClient;
    vertx.fileSystem().readFile("lua/multi_token_bucket.lua", res -> {
      if (res.failed()) {
        completed.fail(res.cause());
        return;
      }
      redisClient.scriptLoad(res.result().toString(), ar -> {
        if (ar.succeeded()) {
          tokenBucketScript = ar.result();
          LOGGER.info("load lua succeeded");
          completed.complete();
        } else {
          LOGGER.error("load lua failed", ar.cause());
          completed.fail(ar.cause());
        }
      });
    });
  }

  @Override
  public void get(String key, Handler<AsyncResult<JsonObject>> handler) {
    redisClient.hgetall(key, handler);
  }

  @Override
  public void set(String key, JsonObject value, Handler<AsyncResult<Void>> handler) {
    redisClient.hmset(key, value, ar -> {
      if (ar.succeeded()) {
        handler.handle(Future.succeededFuture());
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  @Override
  public void setex(String key, JsonObject value, int expires, Handler<AsyncResult<Void>> handler) {
    Future<String> future = Future.future();
    redisClient.hmset(key, value, future.completer());

    future.compose(s -> {
      Future<Long> exFuture = Future.future();
      redisClient.expire(key, expires, exFuture.completer());
      return exFuture;
    }).setHandler(ar -> {
      if (ar.succeeded()) {
        handler.handle(Future.succeededFuture());
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    });

  }

  @Override
  public void delete(String key, Handler<AsyncResult<Void>> handler) {
    redisClient.del(key, ar -> {
      if (ar.succeeded()) {
        handler.handle(Future.succeededFuture());
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  @Override
  public void scriptLoad(String script, Handler<AsyncResult<String>> handler) {
    redisClient.scriptLoad(script, handler);
  }

  @Override
  public void evalsha(String sha1, List<String> keys, List<String> args,
                      Handler<AsyncResult<JsonArray>> handler) {
    redisClient.evalsha(sha1, keys, args, handler);
  }

  @Override
  public void eval(String script, List<String> keys, List<String> args, Handler<AsyncResult<JsonArray>> handler) {
    redisClient.evalsha(script, keys, args, handler);
  }

  @Override
  public void acquireToken(JsonArray rules, Handler<AsyncResult<JsonObject>> handler) {
    if (tokenBucketScript == null) {
      handler.handle(Future.failedFuture("lua is not loaded yet"));
      return;
    }
    if (rules.size() == 0) {
      handler.handle(Future.failedFuture("rules cannot empty"));
    }
    JsonArray limitArray = new JsonArray();
    for (int i = 0; i < rules.size(); i++) {
      JsonObject rule = rules.getJsonObject(i);
      try {
        limitArray.add(new JsonArray().add(rule.getString("subject"))
                               .add(rule.getLong("burst"))
                               .add(rule.getLong("refillTime"))
                               .add(rule.getLong("refillAmount")));
      } catch (Exception e) {
        handler.handle(Future.failedFuture(e));
      }
    }
    List<String> keys = new ArrayList<>();
    List<String> args = new ArrayList<>();
    args.add(limitArray.encode());
    args.add(System.currentTimeMillis() + "");
    args.add("1");
    redisClient.evalsha(tokenBucketScript, keys, args, ar -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
        LOGGER.error("eval lua failed", ar.cause());
        handler.handle(Future.failedFuture("eval lua failed"));
        return;
      }
      createResult(ar.result(), rules, handler);
    });
  }

  private void createResult(JsonArray jsonArray, JsonArray rules,
                           Handler<AsyncResult<JsonObject>> handler) {
    if (jsonArray.size() % 4 != 0) {
      handler.handle(Future.failedFuture("The result must be a multiple of 4"));
    }
    boolean passed = true;
    try {
      List<JsonObject> details = new ArrayList<>();
      for (int i = 0; i < jsonArray.size(); i += 4) {
        Long value = jsonArray.getLong(i) == null ? 0 : jsonArray.getLong(i);
        JsonObject detail = new JsonObject()
                .put("subject", rules.getJsonObject(i % 3).getString("subject"))
                .put("name", rules.getJsonObject(i % 3).getString("name"))
                .put("passed", value == 1)
                .put("remaining", jsonArray.getLong(i + 1))
                .put("limit", jsonArray.getLong(i + 2))
                .put("reset", jsonArray.getLong(i + 3));
        details.add(detail);
        if (value == 0) {
          passed = false;
        }
      }
      JsonObject result = new JsonObject()
              .put("passed", passed)
              .put("details", details);
      handler.handle(Future.succeededFuture(result));
    } catch (Exception e) {
      handler.handle(Future.failedFuture(e));
    }
  }
}
