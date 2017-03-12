package com.edgar.direvolves.plugin.authentication;

import com.edgar.direwolves.core.cache.RedisProvider;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.dispatch.Result;
import com.google.common.base.Strings;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTOptions;
import io.vertx.serviceproxy.ProxyHelper;

import java.util.UUID;

/**
 * 清除token的Filter.
 * 该filter的order=10000
 * Created by edgar on 16-11-26.
 */
public class JwtCleanFilter implements Filter {
  private final Vertx vertx;

  private final String userKey;

  private final RedisProvider redisProvider;

  private final String namespace;

  /**
   * <pre>
   *     - jwt.userClaimKey string token的用户标识
   * </pre>
   *
   * @param vertx  Vertx
   * @param config 配置
   */
  JwtCleanFilter(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    this.userKey = config.getString("jwt.userClaimKey", "userId");
    this.namespace = config.getString("project.namespace", "");
    String address = RedisProvider.class.getName();
    if (!Strings.isNullOrEmpty(namespace)) {
      address = namespace + "." + address;
    }
    this.redisProvider = ProxyHelper.createProxy(RedisProvider.class, vertx, address);
  }

  @Override
  public String type() {
    return POST;
  }

  @Override
  public int order() {
    return 10000;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    if (apiContext.apiDefinition() == null) {
      return false;
    }
    return apiContext.apiDefinition()
                   .plugin(JwtCleanPlugin.class.getSimpleName()) != null;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    int userId = apiContext.principal().getInteger(userKey);
    String userCacheKey = namespace + ":user:" + userId;
    redisProvider.delete(userCacheKey, ar -> {
      completeFuture.complete(apiContext);
    });
  }

}
