package com.edgar.direwolves.plugin.ratelimit;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Created by Edgar on 2017/1/22.
 *
 * @author Edgar  Date 2017/1/22
 */
public class RateLimiterFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(RateLimiterFilter.class);

  private final List<RateLimiterPolicy> policies = new ArrayList<>();

  private final RedisClient redisClient;

  /**
   * 限流脚本
   */
  private String tokenBucketScript;

  RateLimiterFilter(Vertx vertx, JsonObject config) {
    JsonObject redisConfig = config.getJsonObject("redis", new JsonObject());
    redisClient = RedisClient.create(vertx, new RedisOptions(redisConfig));
    vertx.fileSystem().readFile("lua/multi_token_bucket.lua", res -> {
      if (res.failed()) {
        LOGGER.error("read lua failed", res.cause());
        return;
      }
      redisClient.scriptLoad(res.result().toString(), ar -> {
        if (ar.succeeded()) {
          tokenBucketScript = ar.result();
          LOGGER.info("load lua succeeded");
        } else {
          LOGGER.error("load lua failed", ar.cause());
        }
      });
    });

    JsonObject rateLimter = config.getJsonObject("rate.limiter", new JsonObject());
    for (String name : rateLimter.fieldNames()) {
      JsonObject jsonObject = rateLimter.getJsonObject(name);
      String subject = jsonObject.getString("key");
      long limit = jsonObject.getLong("limit");
      long interval = jsonObject.getLong("interval");
      String unit = jsonObject.getString("unit").toUpperCase();
      TimeUnit timeUnit = TimeUnit.valueOf(unit);
      RateLimiterPolicy policy = RateLimiterPolicy.create(name, subject, limit, interval, timeUnit);
      policies.add(policy);
    }
  }

  @Override
  public String type() {
    return PRE;
  }

  @Override
  public int order() {
    return 20000;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    RateLimiterPlugin plugin =
            (RateLimiterPlugin) apiContext.apiDefinition()
                    .plugin(RateLimiterPlugin.class.getSimpleName());
    return plugin != null && !plugin.rateLimiters().isEmpty();
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    RateLimiterPlugin plugin =
            (RateLimiterPlugin) apiContext.apiDefinition()
                    .plugin(RateLimiterPlugin.class.getSimpleName());
    JsonArray limiter = createRule(apiContext, plugin);
    if (limiter.size() == 0) {
      completeFuture.complete(apiContext);
      return;
    }
    acquireToken(limiter, ar -> {
      if (ar.failed()) {
        completeFuture.fail(ar.cause());
        return;
      }
      JsonObject result = ar.result();
      if (result.getBoolean("passed", true)) {
        //增加响应头
        JsonArray details = result.getJsonArray("details", new JsonArray());
        Map<String, Object> ratelimitDetails = ratelimitDetails(limiter, details);
        ratelimitDetails.forEach((k, v) -> apiContext.addVariable(k, v));
        completeFuture.complete(apiContext);
      } else {
        JsonArray details = result.getJsonArray("details", new JsonArray());
        SystemException se = SystemException.create(DefaultErrorCode.TOO_MANY_REQ);
        Map<String, Object> ratelimitDetails = ratelimitDetails(limiter, details);
        ratelimitDetails.forEach((k, v) -> se.set(k, v));
        completeFuture.fail(se);
      }
    });

  }

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

  private Map<String, Object> ratelimitDetails(JsonArray limiters, JsonArray details) {
    Map<String, Object> props = new HashMap<>();
    for (int i = 0; i < details.size(); i++) {
      JsonObject detail = details.getJsonObject(i);
      JsonObject limiter = limiters.getJsonObject(i);
      String name = limiter.getString("name");
      props.put("resp.header:X-Rate-Limit-" + name + "-Limit", detail.getLong("limit"));
      props.put("resp.header:X-Rate-Limit-" + name + "-Remaining", detail.getLong("remaining"));
      props.put("resp.header:X-Rate-Limit-" + name + "-Reset",
                Math.round(detail.getLong("reset") / 1000));
    }

    return props;
  }

  private JsonArray createRule(ApiContext apiContext, RateLimiterPlugin plugin) {
    JsonArray limiter = new JsonArray();
    plugin.rateLimiters().forEach(r -> {
      Optional<RateLimiterPolicy> optional = policies.stream()
              .filter(p -> p.name().equals(r.name()))
              .findFirst();
      if (optional.isPresent()) {
        RateLimiterPolicy policy = optional.get();
        String subject = policy.key();
        if (subject.startsWith("$")) {
          if (apiContext.getValueByKeyword(subject) != null) {
            subject = apiContext.getValueByKeyword(subject).toString();
          } else {
            subject = null;
          }
        }
        if (subject != null) {
          JsonObject rule = new JsonObject()
                  .put("name", r.name())
                  .put("subject", r.name() + "." + subject)
                  .put("refillAmount", policy.limit())
                  .put("burst", r.burst())
                  .put("refillTime", policy.unit().toMillis(policy.interval()));
          limiter.add(rule);
        }
      }
    });
    return limiter;
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
