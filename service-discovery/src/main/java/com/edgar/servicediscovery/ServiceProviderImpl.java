package com.edgar.servicediscovery;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Vert.x官方ServiceDiscovery的扩展,用于增加实现一些特殊的功能.
 *
 * @author Edgar  Date 2017/6/8
 */
public class ServiceProviderImpl implements ServiceProvider {

  private final ServiceDiscovery discovery;

  private final String serviceName;

  private final ProviderStrategy providerStrategy;

  public ServiceProviderImpl(ServiceDiscovery discovery, String serviceName,
                             ProviderStrategy providerStrategy) {
    this.discovery = discovery;
    this.serviceName = serviceName;
    this.providerStrategy = providerStrategy;
  }

  @Override
  public void getInstances(Function<Record, Boolean> filter,
                           Handler<AsyncResult<List<Record>>> handler) {
    discovery.getRecords(r -> r.getName().equalsIgnoreCase(serviceName), ar -> {
      if (ar.failed()) {
        handler.handle(Future.failedFuture(ar.cause()));
        return;
      }
      List<Record> instances = ar.result().stream()
              .filter(i -> filter.apply(i))
              .collect(Collectors.toList());
      handler.handle(Future.succeededFuture(instances));
    });
  }

  @Override
  public void getAllInstances(Handler<AsyncResult<List<Record>>> handler) {
    getInstances(i -> true, handler);
  }

  @Override
  public void getInstance(Function<Record, Boolean> filter,
                          Handler<AsyncResult<Record>> handler) {
    getInstances(filter, ar -> {
      if (ar.failed()) {
        handler.handle(Future.failedFuture(ar.cause()));
        return;
      }
      if (ar.result().isEmpty()) {
        handler.handle(Future.succeededFuture(null));
      }
      List<Record> instances = new CopyOnWriteArrayList<>(ar.result());
      handler.handle(Future.succeededFuture(providerStrategy.get(instances)));
    });
  }
}
