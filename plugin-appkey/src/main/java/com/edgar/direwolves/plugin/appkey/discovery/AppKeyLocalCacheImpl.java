package com.edgar.direwolves.plugin.appkey.discovery;

import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.google.common.base.Ticker;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import java.util.concurrent.TimeUnit;

/**
 * Created by edgar on 17-7-25.
 */
class AppKeyLocalCacheImpl implements AppKeyLocalCache {

  private final Cache<String, JsonObject> cache;

  private final AppKeyDiscovery discovery;

  private final Interner<String> interner = Interners.newWeakInterner();

  public AppKeyLocalCacheImpl(AppKeyDiscovery discovery) {
    this.cache = CacheBuilder.newBuilder()
        .expireAfterWrite(30L, TimeUnit.MINUTES)
        .maximumSize(5000L)
        .ticker(Ticker.systemTicker())
        .build();
    this.discovery = discovery;
  }

  @Override
  public void getAppKey(String appKey, Handler<AsyncResult<AppKey>> resultHandler) {
    synchronized (interner.intern("appkey_" + appKey)) {
      JsonObject jsonObject = cache.getIfPresent(appKey);
      if (jsonObject != null) {
        resultHandler.handle(Future.succeededFuture(new AppKey(appKey, jsonObject)));
        return;
      }
      discovery.getAppKey(appKey, ar -> {
        if (ar.failed()) {
          resultHandler.handle(Future.failedFuture(ar.cause()));
          return;
        }
        if (ar.result() != null) {
          cache.put(ar.result().getAppkey(), ar.result().getJsonObject());
          resultHandler.handle(Future.succeededFuture(new AppKey(ar.result().getAppkey(), ar.result().getJsonObject())));
          return;
        }
        if (ar.result() == null) {
          resultHandler.handle(Future.failedFuture(SystemException.create(DefaultErrorCode.RESOURCE_NOT_FOUND)
                                                           .set("details", "Undefined AppKey:" + appKey)));
          return;
        }
      });
    }

  }

  @Override
  public int size() {
    return 0;
  }
}
