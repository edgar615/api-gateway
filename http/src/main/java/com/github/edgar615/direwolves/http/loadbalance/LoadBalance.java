package com.github.edgar615.direwolves.http.loadbalance;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.servicediscovery.Record;

/**
 * Created by Edgar on 2017/7/31.
 *
 * @author Edgar  Date 2017/7/31
 */
public interface LoadBalance {

  void chooseServer(String service, Handler<AsyncResult<Record>> resultHandler);

  /**
   * config的配置：
   * "strategy": {
   * "user": "random",
   * "device": "round_robin"
   * }
   *
   * @param serviceFinder
   * @param options
   * @return
   */
  static LoadBalance create(ServiceFinder serviceFinder, LoadBalanceOptions options) {
    return new LoadBalanceImpl(serviceFinder, options);
  }
}
