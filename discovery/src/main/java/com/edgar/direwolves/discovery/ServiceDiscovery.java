package com.edgar.direwolves.discovery;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.function.Function;

/**
 * Vert.x提供对Service Discovery并不能很好的实现动态权重，所以使用自己实现的Service Discovery.
 * 原理和Service Disovery差不多.
 * Created by edgar on 17-5-9.
 */
public interface ServiceDiscovery {


  List<ServiceInstance> getInstances();

  List<ServiceInstance> getInstances(Function<ServiceInstance, Boolean> filter);

  ServiceInstance getInstance(String name);

  void complete(String id, long duration);

  void fail(String id);

  static ServiceDiscovery create(Vertx vertx, JsonObject config) {
    return new ServiceDiscoveryImpl(vertx, config);
  }
}
