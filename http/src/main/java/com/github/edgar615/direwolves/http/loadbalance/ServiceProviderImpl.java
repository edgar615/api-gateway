package com.github.edgar615.direwolves.http.loadbalance;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.servicediscovery.Record;

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

  private final ServiceFinder serviceFinder;

  private final String service;

//  private final ServiceFilter circuitBreakerFilter = r ->
//          !LoadBalanceStats.instance().get(r.getRegistration()).isCircuitBreakerTripped();

  private ChooseStrategy strategy = ChooseStrategy.roundRobin();

  ServiceProviderImpl(ServiceFinder serviceFinder, String service) {
    this.serviceFinder = serviceFinder;
    this.service = service;
//    filters.add(circuitBreakerFilter);
  }

  @Override
  public ServiceProvider withStrategy(ChooseStrategy strategy) {
    Objects.requireNonNull(strategy, "ChooseStrategy");
    this.strategy = strategy;
    return this;
  }

  @Override
  public ServiceProvider addFilter(ServiceFilter filter) {
    Objects.requireNonNull(filter, "ServiceFilter");
    filters.add(filter);
    return this;
  }

  @Override
  public void choose(Handler<AsyncResult<Record>> resultHandler) {
    Function<Record, Boolean> accept = r -> r.getName().equals(service)
                                            && filters.stream().allMatch(f -> f.apply(r));
    serviceFinder.getRecords(accept, ar -> {
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
