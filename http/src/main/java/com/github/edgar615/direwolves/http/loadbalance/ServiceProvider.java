package com.github.edgar615.direwolves.http.loadbalance;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.servicediscovery.Record;

/**
 * Created by Edgar on 2017/7/28.
 *
 * @author Edgar  Date 2017/7/28
 */
public interface ServiceProvider {

  static ServiceProvider create(ServiceFinder serviceFinder, String service) {
    return new ServiceProviderImpl(serviceFinder, service);
  }

  ServiceProvider withStrategy(ChooseStrategy strategy);

  ServiceProvider addFilter(ServiceFilter filter);

  void choose(Handler<AsyncResult<Record>> resultHandler);
}
