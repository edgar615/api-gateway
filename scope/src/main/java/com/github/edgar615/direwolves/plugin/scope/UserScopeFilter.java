package com.github.edgar615.direwolves.plugin.scope;

import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.direwolves.core.utils.CacheUtils;
import com.github.edgar615.direwolves.core.utils.Consts;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import com.github.edgar615.util.log.Log;
import com.github.edgar615.util.vertx.cache.Cache;
import com.github.edgar615.util.vertx.cache.CacheLoader;
import com.github.edgar615.util.vertx.redis.RedisClientHelper;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 权限校验的filter.
 *
 * 如果接口包括AppKeyPlugin插件，那么在AppKeyFilter调用之后会在上下文中存入<b>app.permissions</b>变量
 * 如果接口包括Authentication插件，那么在AuthenticationFilter调用之后会在用户属性中存入<b>permissions</b>变量
 * 如果调用方或者用户没有对应的权限，直接返回1004的错误.
 * <p>
 *
 *   该filter的order=1100
 */
public class UserScopeFilter implements Filter {
  private static final Logger LOGGER = LoggerFactory.getLogger(UserScopeFilter.class);
  private final String namespace;

  private final Vertx vertx;

  private final Cache<String, JsonObject> cache;

  private final CacheLoader<String, JsonObject> appKeyLoader;

  private final RedisClient redisClient;

  UserScopeFilter(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    this.namespace = config.getString("namespace", Consts.DEFAULT_NAMESPACE);
    JsonObject appKeyConfig = config.getJsonObject("appkey", new JsonObject());
    if (appKeyConfig.getValue("cache") instanceof JsonObject) {
      this.cache = CacheUtils.createCache(vertx, "appKeyCache",
                                          appKeyConfig.getJsonObject("cache"));
    } else {
      this.cache = CacheUtils.createCache(vertx, "appKeyCache", new JsonObject());
    }

    this.redisClient = RedisClientHelper.getShared(vertx);
//    RedisCache redisCache =  new RedisCache(redisClient, cacheName, options);

    appKeyConfig.put("port", config.getInteger("port", Consts.DEFAULT_PORT));
    appKeyLoader = new CacheLoader<String, JsonObject>() {
      @Override
      public void load(String key, Handler<AsyncResult<JsonObject>> handler) {

      }
    };

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
    return apiContext.apiDefinition().plugin(ScopePlugin.class.getSimpleName()) != null
            && apiContext.principal() != null;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    ScopePlugin plugin = (ScopePlugin) apiContext.apiDefinition()
            .plugin(ScopePlugin.class.getSimpleName());
    String apiScope = plugin.scope();
    String userId = apiContext.principal().getValue("userId").toString();
    String cacheKey = "user:permission:" + userId;
    redisClient.hget(cacheKey, apiScope, ar -> {
      if (ar.succeeded()) {
        if (ar.result().equals("1")) {
          //通过
          completeFuture.complete(apiContext);
        } else if (ar.result().equals("0")) {
          //不通过
          Log.create(LOGGER)
                  .setTraceId(apiContext.id())
                  .setEvent("user.scope.failed")
                  .warn();
          SystemException ex = SystemException.create(DefaultErrorCode.PERMISSION_DENIED)
            .set("details", "User does not have permission");
          completeFuture.fail(ex);
        } else {
          //没找到对应的数据，通过URL检查
          HttpScopeLoader loader = new HttpScopeLoader(vertx, new JsonObject());
          loader.load(userId, apiScope, loadResult -> {
            String pass = "0";
            if (loadResult.succeeded()) {
              pass = "1";
            }
            redisClient.hset(cacheKey, apiScope, pass, Future.<Long>future().completer());
            //设置过期时间
            redisClient.expire(cacheKey, 30 * 60l, Future.<Long>future().completer());
          });
        }
      } else {
        //降级通过URL检查
      }
    });
  }

}