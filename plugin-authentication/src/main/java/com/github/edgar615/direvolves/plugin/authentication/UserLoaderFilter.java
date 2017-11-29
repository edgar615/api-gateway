package com.github.edgar615.direvolves.plugin.authentication;

import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.direwolves.core.utils.CacheUtils;
import com.github.edgar615.util.log.Log;
import com.github.edgar615.util.vertx.cache.Cache;
import com.github.edgar615.util.vertx.cache.CacheLoader;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * 根据userId从下游服务中拉取用户信息，并且更新到principal
 * * 该filter可以接受下列的配置参数
 * <pre>
 *   namespace 项目的命名空间，用来避免多个项目冲突，默认值""
 * </pre>
 * user
 * <pre>
 *     "keyStore" : {
 * "path": "keystore.jceks", 证书路径
 * "type": "jceks", 证书类型
 * "password": "secret" 证书密码
 * }
 * </pre>
 * 该filter的order=10000
 * Created by edgar on 16-11-26.
 */
public class UserLoaderFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserLoaderFilter.class);

  private final Vertx vertx;

  private final String userKey = "userId";

  private final String namespace;

  private final Cache<String, JsonObject> cache;

  private final CacheLoader<String, JsonObject> userLoader;

  private final String NOT_EXISTS_KEY = UUID.randomUUID().toString();

  /**
   * @param vertx  Vertx
   * @param config 配置
   */
  UserLoaderFilter(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    this.namespace = config.getString("namespace", "api-gateway");
    JsonObject userConfig = config.getJsonObject("user", new JsonObject());
    if (userConfig.getValue("cache") instanceof JsonObject) {
      this.cache = CacheUtils.createCache(vertx, "userCache",
                                          userConfig.getJsonObject("cache"));
    } else {
      this.cache = CacheUtils.createCache(vertx, "userCache", new JsonObject());
    }

    if (userConfig.getValue("loader") instanceof String) {
      JsonObject httpConfig = new JsonObject();
      httpConfig.put("port", config.getInteger("port", 9000));
      httpConfig.put("url", userConfig.getString("loader"));
      httpConfig.put("notExistsKey", NOT_EXISTS_KEY);
      userLoader = new UserLoader(vertx, namespace + ":u:", httpConfig);
    } else {
      userLoader = null;
    }

  }

  @Override
  public String type() {
    return PRE;
  }

  @Override
  public int order() {
    return 11000;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    if (apiContext.principal() == null) {
      return false;
    }
    return apiContext.principal().containsKey(userKey)
            && userLoader != null;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    Object userId = apiContext.principal().getValue(userKey);
    cache.get(wrapKey(userId), userLoader, ar -> {
      if (ar.failed() || ar.result().containsKey(NOT_EXISTS_KEY)) {
        Log.create(LOGGER)
                .setTraceId(apiContext.id())
                .setEvent("user.undefined")
                .addData("user", userId)
                .error();
      } else {
        apiContext.principal().mergeIn(ar.result());
      }
      completeFuture.complete(apiContext);
    });
  }

  private String wrapKey(Object userId) {
    return namespace + ":u:" + userId.toString();
  }
}
