package com.edgar.direwolves.loadbalance;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;

import java.util.concurrent.ExecutionException;

/**
 * Created by Edgar on 2017/7/31.
 *
 * @author Edgar  Date 2017/7/31
 */
class LoadBalanceImpl implements LoadBalance {

  private final JsonObject config;

  private ServiceCache serviceCache;

  private LoadingCache<String, ServiceProvider> providerCache;

  LoadBalanceImpl(ServiceCache serviceCache, JsonObject config) {
    this.config = config;
    this.serviceCache = serviceCache;
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
    JsonObject strategyConfig = config.getJsonObject("strategy", new JsonObject());
    String strategyName = strategyConfig.getString(service, "round_robin");
    ChooseStrategy strategy = ChooseStrategy.roundRobin();
    if ("random".equalsIgnoreCase(strategyName)) {
      strategy = ChooseStrategy.random();
    }
    if ("round_robin".equalsIgnoreCase(strategyName)) {
      strategy = ChooseStrategy.roundRobin();
    }
    if ("sticky".equalsIgnoreCase(strategyName)) {
      strategy = ChooseStrategy.sticky(ChooseStrategy.roundRobin());
    }
    if ("weight_round_robin".equalsIgnoreCase(strategyName)) {
      strategy = ChooseStrategy.weightRoundRobin();
    }
    return new ServiceProviderImpl(serviceCache, service)
            .withStrategy(strategy);
  }

  private ServiceProvider getProvider(String service) {
    try {
      return providerCache.get(service);
    } catch (ExecutionException e) {
      ServiceProvider provider = ServiceProvider.create(serviceCache, service);
      providerCache.asMap().putIfAbsent(service, provider);
      return providerCache.asMap().get(service);
    }
  }
}
