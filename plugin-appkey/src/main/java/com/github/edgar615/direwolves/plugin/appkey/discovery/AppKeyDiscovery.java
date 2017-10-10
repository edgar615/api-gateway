package com.github.edgar615.direwolves.plugin.appkey.discovery;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/7/20.
 *
 * @author Edgar  Date 2017/7/20
 */
public interface AppKeyDiscovery extends AppKeyPublisher {

  static AppKeyDiscovery create(Vertx vertx, String name) {
    return new AppKeyDiscoveryImpl(vertx, name);
  }

  void getAppKey(String appKey,
                     Handler<AsyncResult<AppKey>> resultHandler);

  AppKeyDiscovery registerImporter(AppKeyImporter importer, JsonObject config,
                                   Handler<AsyncResult<Void>> completionHandler);

  void close();
}
