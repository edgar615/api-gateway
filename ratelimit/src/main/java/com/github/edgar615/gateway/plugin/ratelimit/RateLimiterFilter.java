package com.github.edgar615.gateway.plugin.ratelimit;

import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.gateway.core.dispatch.Filter;
import com.github.edgar615.gateway.core.utils.Consts;
import com.github.edgar615.util.log.Log;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import com.github.edgar615.util.vertx.redis.RedisClientHelper;
import com.github.edgar615.util.vertx.redis.ratelimit.LimitResult;
import com.github.edgar615.util.vertx.redis.ratelimit.MultiTokenBucket;
import com.github.edgar615.util.vertx.redis.ratelimit.ResultDetail;
import com.github.edgar615.util.vertx.redis.ratelimit.TokenBucketRule;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 基于redis的限流.
 * <p>
 * //RedisVerticle 要先部署
 *
 * @author Edgar  Date 2017/1/22
 */
public class RateLimiterFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(RateLimiterFilter.class);

  private final List<RateLimiterPolicy> policies = new ArrayList<>();

  private final MultiTokenBucket tokenBucket;

  private AtomicBoolean luaLoaded = new AtomicBoolean();

  RateLimiterFilter(Vertx vertx, JsonObject config) {
    RedisClient redisClient = RedisClientHelper.getShared(vertx);
    Future<Void> future = Future.future();
    tokenBucket = new MultiTokenBucket(vertx, redisClient, future);
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        //todo
        Log.create(LOGGER)
                .setEvent("ratelimiter.init.succeed")
                .info();
        luaLoaded.set(true);
      } else {
        //todo
        Log.create(LOGGER)
                .setEvent("ratelimiter.init.succeed")
                .error();
      }
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
    return plugin != null && !plugin.rateLimiters().isEmpty()
           && luaLoaded.get();
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    RateLimiterPlugin plugin =
            (RateLimiterPlugin) apiContext.apiDefinition()
                    .plugin(RateLimiterPlugin.class.getSimpleName());
    List<TokenBucketRule> rules = createRule(apiContext, plugin);
    if (rules.size() == 0) {
      completeFuture.complete(apiContext);
      return;
    }

    tokenBucket.tokenBucket(1, rules, ar -> {
      if (ar.failed()) {
        completeFuture.fail(ar.cause());
        return;
      }
      LimitResult result = ar.result();
      if (result.passed()) {
        //增加响应头
        Map<String, Object> ratelimitDetails = ratelimitDetails(rules, result.details());
        ratelimitDetails.forEach((k, v) -> apiContext.addVariable(k, v));
        completeFuture.complete(apiContext);
      } else {
        LOGGER.warn("[{}] [RateLimiterFilter] [{}] [{}]", apiContext.id(), "RateLimiterTripped",
                    result.details());
        SystemException se = SystemException.create(DefaultErrorCode.TOO_MANY_REQ);
        Map<String, Object> ratelimitDetails = ratelimitDetails(rules, result.details());
        ratelimitDetails.forEach((k, v) -> se.set(k, v));
        completeFuture.fail(se);
      }
    });


  }


  private Map<String, Object> ratelimitDetails(List<TokenBucketRule> rules,
                                               List<ResultDetail> details) {
    Map<String, Object> props = new HashMap<>();
    for (int i = 0; i < details.size(); i++) {
      ResultDetail detail = details.get(i);
      TokenBucketRule limiter = rules.get(i);
//      String name = limiter.getSubject();
      props.put(Consts.RESPONSE_HEADER + "X-Rate-Limit-" + i + "-Limit", detail.limit());
      props.put(Consts.RESPONSE_HEADER + "X-Rate-Limit-"  + i + "-Remaining", detail.remaining());
      props.put(Consts.RESPONSE_HEADER + "X-Rate-Limit-"  + i + "-Reset",
                Math.round(detail.reset() / 1000));
    }

    return props;
  }

  private List<TokenBucketRule> createRule(ApiContext apiContext, RateLimiterPlugin plugin) {
    List<TokenBucketRule> rules = new ArrayList<>();
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
          TokenBucketRule rule =
                  new TokenBucketRule(r.name() + "." + subject)
                          .setBurst(r.burst())
                          .setRefillTime(policy.unit().toMillis(policy.interval()))
                          .setRefillAmount(policy.limit());
          rules.add(rule);
        }
      }
    });
    return rules;
  }

}
