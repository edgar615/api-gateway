package com.edgar.direwolves.loadbalance;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Created by Edgar on 2017/7/28.
 *
 * @author Edgar  Date 2017/7/28
 */
class ServiceProviderImpl implements ServiceProvider {

  private final List<ServiceFilter> filters = new ArrayList<>();

  private final ServiceCache cache;

  private final String service;

  private ChooseStrategy strategy = ChooseStrategy.roundRobin();

  ServiceProviderImpl(ServiceCache cache, String service) {
    this.cache = cache;
    this.service = service;
  }

  public ServiceProvider withStrategy(ChooseStrategy strategy) {
    Objects.requireNonNull(strategy, "ChooseStrategy");
    this.strategy = strategy;
    return this;
  }

  public ServiceProvider addFilter(ServiceFilter filter) {
    Objects.requireNonNull(filter, "ServiceFilter");
    filters.add(filter);
    return this;
  }

  @Override
  public void choose(Handler<AsyncResult<Record>> resultHandler) {
    Function<Record, Boolean> accept = r -> r.getName().equals(service)
                                            && filters.stream().allMatch(f -> f.apply(r));
    cache.getRecords(accept, ar -> {
      if (ar.failed()) {
        resultHandler.handle(Future.failedFuture(ar.cause()));
        return;
      }
      if (ar.result().size() == 0) {
        resultHandler.handle(Future.failedFuture(new ServiceNotFoundException(service)));
        return;
      }
      Record record = strategy.get(ar.result());
      if (record == null) {
        resultHandler.handle(Future.failedFuture(new ServiceNotFoundException(service)));
        return;
      }
      resultHandler.handle(Future.succeededFuture(record));
    });
  }
}
