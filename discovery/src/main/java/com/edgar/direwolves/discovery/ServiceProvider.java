package com.edgar.direwolves.discovery;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.ServiceDiscovery;

import java.util.List;
import java.util.function.Function;

/**
 * Created by edgar on 17-5-9.
 */
public interface ServiceProvider {


  List<ServiceInstance> getInstances();

  List<ServiceInstance> getInstances(Function<ServiceInstance, Boolean> filter);

  ServiceInstance getInstance(String name);

  void complete(String id, long duration);

  void fail(String id);

  static ServiceProvider create(Vertx vertx, JsonObject config) {
    return new ServiceProviderImpl(vertx, config);
  }
}
