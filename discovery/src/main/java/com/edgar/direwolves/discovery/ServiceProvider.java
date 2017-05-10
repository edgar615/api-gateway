package com.edgar.direwolves.discovery;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.List;
import java.util.function.Function;

/**
 * Created by edgar on 17-5-9.
 */
public interface ServiceProvider {


  List<ServiceInstance> getInstances();

  List<ServiceInstance> getInstances(Function<ServiceInstance, Boolean> filter);

  ServiceInstance getInstance();

  ServiceInstance getInstance(Function<ServiceInstance, Boolean> filter);

  ServiceInstance getInstance(String name);
}
