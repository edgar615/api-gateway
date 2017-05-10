package com.edgar.direwolves.discovery;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.List;
import java.util.function.Function;

/**
 * Created by edgar on 17-5-9.
 */
public interface ServiceProvider {

  void getInstances(Handler<AsyncResult<List<ServiceInstance>>> handler);

  void getInstances(Function<ServiceInstance, Boolean> filter,
                    Handler<AsyncResult<List<ServiceInstance>>> handler);

  void getInstance(Handler<AsyncResult<ServiceInstance>> handler);

  void getInstance(Function<ServiceInstance, Boolean> filter,
                   Handler<AsyncResult<ServiceInstance>> handler);

  void getInstance(String name,
                   Handler<AsyncResult<ServiceInstance>> handler);

}
