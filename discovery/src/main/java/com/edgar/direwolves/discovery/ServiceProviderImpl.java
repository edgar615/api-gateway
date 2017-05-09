package com.edgar.direwolves.discovery;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.servicediscovery.impl.AsyncMap;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by edgar on 17-5-9.
 */
class ServiceProviderImpl implements ServiceProvider {
  private final Vertx vertx;
  private final AsyncMap<String, ServiceInstance> instances;

  ServiceProviderImpl(Vertx vertx, String name) {
    this.vertx = vertx;
    this.instances = new AsyncMap<>(vertx, name);
  }

  @Override
  public void getInstances(Handler<AsyncResult<List<ServiceInstance>>> handler) {
    getInstances(i -> true, handler);
  }

  @Override
  public void getInstances(Function<ServiceInstance, Boolean> filter, Handler<AsyncResult<List<ServiceInstance>>> handler) {
    instances.getAll(ar -> {
      if (ar.succeeded()) {
        handler.handle(Future.succeededFuture(ar.result().values().stream()
            .filter(i -> filter.apply(i))
            .collect(Collectors.toList())));
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  @Override
  public void getInstance(Handler<AsyncResult<ServiceInstance>> handler) {
    getInstance(r -> true, handler);
  }

  @Override
  public void getInstance(Function<ServiceInstance, Boolean> filter, Handler<AsyncResult<ServiceInstance>> handler) {
    instances.getAll(ar -> {
      if (ar.succeeded()) {
        handler.handle(Future.succeededFuture(ar.result().values().stream()
            .filter(i -> filter.apply(i))
            .findAny().get()));
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

}
