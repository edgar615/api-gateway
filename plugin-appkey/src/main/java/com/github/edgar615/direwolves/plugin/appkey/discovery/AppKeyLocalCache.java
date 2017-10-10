package com.github.edgar615.direwolves.plugin.appkey.discovery;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

/**
 * Created by edgar on 17-7-25.
 */
public interface AppKeyLocalCache {
  void getAppKey(String appKey,
                 Handler<AsyncResult<AppKey>> resultHandler);

  int size();

  static AppKeyLocalCache create(Vertx vertx, AppKeyDiscovery discovery) {
    return new AppKeyLocalCacheImpl(vertx, discovery);
  }
}
