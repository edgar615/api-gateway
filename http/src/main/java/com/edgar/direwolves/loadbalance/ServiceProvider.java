package com.edgar.direwolves.loadbalance;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;

/**
 * Created by Edgar on 2017/7/28.
 *
 * @author Edgar  Date 2017/7/28
 */
public interface ServiceProvider {

  static ServiceProvider create(ServiceCache serviceCache, String service) {
    return new ServiceProviderImpl(serviceCache, service);
  }

  ServiceProvider withStrategy(ChooseStrategy strategy);

  ServiceProvider addFilter(ServiceFilter filter);

  void choose(Handler<AsyncResult<Record>> resultHandler);
}
