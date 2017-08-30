package com.edgar.direvolves.plugin.authentication;

import com.edgar.direwolves.core.cache.Cache;
import com.edgar.direwolves.core.cache.CacheManager;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 清除token的Filter.
 * 该filter的order=10000
 * Created by edgar on 16-11-26.
 */
public class JwtCleanFilter implements Filter {
  private static final Logger LOGGER = LoggerFactory.getLogger(JwtCleanFilter.class);

  private final Vertx vertx;

  private final String userKey;

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
    this.namespace = config.getString("namespace", "");
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
    String userId = apiContext.principal().getValue(userKey).toString();
    //如果把userCache放在构造函数中初始化，可能会出现没有userCache的情况
    Cache userCache = CacheManager.instance().getCache("userCache");
    String userCacheKey = namespace + ":user:" + userId;
    userCache.evict(userCacheKey, ar -> {
      LOGGER.info("---| [{}] [OK] [{}] [{}]",
                  apiContext.id(),
                  this.getClass().getSimpleName(),
                  "delete token:" + userCacheKey);
      completeFuture.complete(apiContext);
    });
  }

}
