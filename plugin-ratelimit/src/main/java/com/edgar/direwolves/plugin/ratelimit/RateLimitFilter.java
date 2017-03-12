package com.edgar.direwolves.plugin.ratelimit;

import com.google.common.base.Strings;

import com.edgar.direwolves.core.cache.RedisProvider;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ProxyHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by Edgar on 2017/1/22.
 *
 * @author Edgar  Date 2017/1/22
 */
public class RateLimitFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitFilter.class);

  private final String namespace;

  private final RedisProvider redisProvider;

  private final Vertx vertx;

  private final String ratelimitScriptPath;

  private String scriptSha1;

  RateLimitFilter(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    this.ratelimitScriptPath = config.getString("lua.ratelimit.path", "ratelimit.lua");
    this.namespace = config.getString("project.namespace", "");
    String address = RedisProvider.class.getName();
    if (!Strings.isNullOrEmpty(namespace)) {
      address = namespace + "." + address;
    }
    this.redisProvider = ProxyHelper.createProxy(RedisProvider.class, vertx, address);
    String script = vertx.fileSystem().readFileBlocking(ratelimitScriptPath).toString();
    redisProvider.scriptLoad(script, ar -> {
      if (ar.succeeded()) {
        scriptSha1 = ar.result();
        LOGGER.info("load ratelimiter lua succeed, sha1->{}", scriptSha1);
      } else {
        LOGGER.error("load ratelimiter lua failed, sha1->{}", ar.cause());
      }
    });
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
    RateLimitPlugin plugin =
            (RateLimitPlugin) apiContext.apiDefinition()
                    .plugin(RateLimitPlugin.class.getSimpleName());
    return plugin != null && !plugin.rateLimits().isEmpty();
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    if (Strings.isNullOrEmpty(scriptSha1)) {
      completeFuture.complete(apiContext);
    } else {

      RateLimitPlugin plugin =
              (RateLimitPlugin) apiContext.apiDefinition()
                      .plugin(RateLimitPlugin.class.getSimpleName());
      List<RateLimit> rateLimits = plugin.rateLimits();
      for (RateLimit rateLimit : rateLimits) {
        String type = rateLimit.type();
        long limit = rateLimit.limit();
        double rate = 0;
//        if ("second".equalsIgnoreCase(type)) {
//          rate = limit;
//        } else ("minute".equalsIgnoreCase(type)) {
//          rate = limit / 60;
//        }
      }

    }
  }
}
