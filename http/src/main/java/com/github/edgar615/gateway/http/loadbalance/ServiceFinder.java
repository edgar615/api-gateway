package com.github.edgar615.gateway.http.loadbalance;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;

import java.util.List;
import java.util.function.Function;

/**
 * 用于提高服务发现性能的本地缓存.
 *
 * @author Edgar  Date 2017/7/31
 */
public interface ServiceFinder {

  void getRecord(Function<Record, Boolean> filter,
                 Handler<AsyncResult<Record>> resultHandler);

  void getRecords(Function<Record, Boolean> filter,
                  Handler<AsyncResult<List<Record>>> resultHandler);

  void reload(Handler<AsyncResult<List<Record>>> resultHandler);


  static ServiceFinder create(Vertx vertx, ServiceDiscovery discovery) {
    return new ServiceFinderImpl(vertx, discovery);
  }

}
