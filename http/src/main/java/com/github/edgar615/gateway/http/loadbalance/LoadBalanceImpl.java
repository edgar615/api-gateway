package com.github.edgar615.gateway.http.loadbalance;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.servicediscovery.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

/**
 * Created by Edgar on 2017/7/31.
 *
 * @author Edgar  Date 2017/7/31
 */
class LoadBalanceImpl implements LoadBalance {

  /**
   * 服务节点的状态集合
   */
  private final LoadBalanceStats stats;

  private final LoadBalanceOptions options;

  /**
   * 服务发现的本地缓存
   */
  private ServiceFinder serviceFinder;

  private LoadingCache<String,ChooseStrategy> strategyCache;

  LoadBalanceImpl(ServiceFinder serviceFinder, LoadBalanceOptions options) {
    this.serviceFinder = serviceFinder;
    this.stats = LoadBalanceStats.instance();
    this.options = options;

    this.strategyCache = CacheBuilder.newBuilder()
            .build(new CacheLoader<String, ChooseStrategy>() {
              @Override
              public ChooseStrategy load(String service) throws Exception {
                return createStrategy(service);
              }
            });
  }

  @Override
  public void chooseServer(String service, Handler<AsyncResult<Record>> resultHandler) {
      chooseServer(service, new ArrayList<>(), resultHandler);
  }

  @Override
  public void chooseServer(String service, List<ServiceFilter> filters, Handler<AsyncResult<Record>> resultHandler) {
        Function<Record, Boolean> accept = r -> r.getName().equals(service)
            && filters.stream().allMatch(f -> f.apply(r));
    ChooseStrategy strategy = getStrategy(service);
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


  private ChooseStrategy createStrategy(String service) {
    return options.getStrategies().getOrDefault(service, ChooseStrategy.roundRobin());
  }

  private ChooseStrategy getStrategy(String service) {
    try {
      return strategyCache.get(service);
    } catch (ExecutionException e) {
      ChooseStrategy strategy = createStrategy(service);
      strategyCache.asMap().putIfAbsent(service, strategy);
      return strategyCache.asMap().get(service);
    }
  }
}
