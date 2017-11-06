package com.github.edgar615.direwolves.http.loadbalance;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.servicediscovery.Record;

import java.util.concurrent.ExecutionException;

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

  private LoadingCache<String, ServiceProvider> providerCache;

  LoadBalanceImpl(ServiceFinder serviceFinder, LoadBalanceOptions options) {
    this.serviceFinder = serviceFinder;
    this.stats = LoadBalanceStats.instance();
    this.options = options;
    this.providerCache = CacheBuilder.newBuilder()
            .build(new CacheLoader<String, ServiceProvider>() {
              @Override
              public ServiceProvider load(String service) throws Exception {
                return createProvider(service);
              }
            });
  }

  @Override
  public void chooseServer(String service, Handler<AsyncResult<Record>> resultHandler) {
    getProvider(service).choose(resultHandler);
  }

  private ServiceProvider createProvider(String service) {
    ChooseStrategy strategy = options.getStrategy(service);
    return new ServiceProviderImpl(serviceFinder, service)
            .withStrategy(strategy)
            .addFilter(r -> !stats.get(r.getRegistration()).isCircuitBreakerTripped());
  }

  private ServiceProvider getProvider(String service) {
    try {
      return providerCache.get(service);
    } catch (ExecutionException e) {
      ServiceProvider provider = createProvider(service);
      providerCache.asMap().putIfAbsent(service, provider);
      return providerCache.asMap().get(service);
    }
  }
}
