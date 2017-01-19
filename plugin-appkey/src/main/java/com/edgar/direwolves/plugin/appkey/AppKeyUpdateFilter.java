package com.edgar.direwolves.plugin.appkey;

import com.edgar.direwolves.core.cache.RedisProvider;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.dispatch.Result;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ProxyHelper;

import java.util.UUID;

/**
 * 由于目前缺少有效的机制更新appKey，目前采用一种临时方案处理appkey的更新.
 * 目前的服务需要OEM用户在首次使用时通过OEM激活，这时会返回对应的appKey和密钥，在这时候我们在讲appKey保存到redis中.
 * <p>
 * <pre>
 *   project.namespace 项目的命名空间，用来避免多个项目冲突，默认值""
 *   service.cache.address 缓存的地址，默认值direwolves.cache.provider
 *
 *  app.key 激活信息中appKey的键值，默认值appKey
 * </pre>
 * 该filter的order=10000
 * Created by edgar on 16-11-26.
 */
public class AppKeyUpdateFilter implements Filter {
  private final Vertx vertx;

  private final String appKey;

  private final RedisProvider redisProvider;

  private final String namespace;

  AppKeyUpdateFilter(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    this.namespace = config.getString("project.namespace", "");
    String address = config.getString("service.cache.address", "direwolves.cache.provider");
    this.redisProvider = ProxyHelper.createProxy(RedisProvider.class, vertx, address);
    this.appKey = config.getString("app.key", "appKey");
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
                   .plugin(AppKeyUpdatePlugin.class.getSimpleName()) != null;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    Result result = apiContext.result();
    if (!result.isArray()
        && result.statusCode() < 400
        && result.responseObject().containsKey(appKey)) {
      JsonObject body = result.responseObject();
      String app = body.getString(appKey);
      String appCacheKey = namespace + ":appKey:" + app;
      JsonObject appJson = body.copy();
      redisProvider.set(appCacheKey, appJson, ar -> {
        if (ar.succeeded()) {
          try {
            completeFuture.complete(apiContext);
          } catch (Exception e) {
            completeFuture.fail(e);
          }
        } else {
          completeFuture.fail(ar.cause());
        }
      });

    } else {
      completeFuture.complete(apiContext);
    }
  }

}
