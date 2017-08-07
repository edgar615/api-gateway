package com.edgar.direwolves.loadbalance;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;

import java.util.List;
import java.util.function.Function;

/**
 * 服务发现的本地缓存.
 *
 * @author Edgar  Date 2017/7/31
 */
public interface ServiceCache {

  void getRecord(Function<Record, Boolean> filter,
                 Handler<AsyncResult<Record>> resultHandler);

  void getRecords(Function<Record, Boolean> filter,
                  Handler<AsyncResult<List<Record>>> resultHandler);

  static ServiceCache create(Vertx vertx, ServiceDiscovery discovery) {
    return new ServiceCacheImpl(vertx, discovery);
  }

  static ServiceCache create(Vertx vertx, ServiceDiscoveryOptions options) {
    return new ServiceCacheImpl(vertx, ServiceDiscovery.create(vertx, options));
  }

  /**
   * config包括下列属性：
   * announceAddress String
   * usageAddress String
   * autoRegistrationOfImporters boolean
   * backendConfiguration JsonObject
   * name String
   *
   * @param vertx
   * @param config
   * @return
   */
  static ServiceCache create(Vertx vertx, JsonObject config) {
    return new ServiceCacheImpl(vertx,
                                ServiceDiscovery.create(vertx,
                                                        new ServiceDiscoveryOptions(config)));
  }
}
