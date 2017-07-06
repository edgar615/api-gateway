package com.edgar.servicediscovery;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.ServiceDiscovery;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Edgar on 2017/6/8.
 *
 * @author Edgar  Date 2017/6/8
 */
public class ServiceProviderRegistry  {

  /**
   * 服务发现策略
   */
  private final JsonObject config;

  private final Vertx vertx;

  private final Map<String, ServiceProvider> providerMap = new ConcurrentHashMap<>();

  private final ServiceDiscovery discovery;

  public static  ServiceProviderRegistry create(Vertx vertx, JsonObject config) {
    return new ServiceProviderRegistry(vertx, config);
  }


  ServiceProviderRegistry(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    this.config = config;
    this.discovery = ServiceDiscovery.create(vertx);
  }

  public ServiceProvider get(String name) {
    return providerMap.computeIfAbsent(name, k -> createProvider(name));
  }

  private ServiceProvider createProvider(String serviceName) {
    JsonObject strategyConfig = config.getJsonObject("strategy", new JsonObject());
    String strategyName = strategyConfig.getString(serviceName, "round_robin");
    ProviderStrategy strategy = ProviderStrategy.roundRobin();
    if ("random".equalsIgnoreCase(strategyName)) {
      strategy = ProviderStrategy.random();
    }
    if ("round_robin".equalsIgnoreCase(strategyName)) {
      strategy = ProviderStrategy.roundRobin();
    }
    if ("sticky".equalsIgnoreCase(strategyName)) {
      strategy = ProviderStrategy.sticky(ProviderStrategy.roundRobin());
    }
    if ("weight_round_robin".equalsIgnoreCase(strategyName)) {
      strategy = ProviderStrategy.weightRoundRobin();
    }
    return new ServiceProviderImpl(this.discovery, serviceName, strategy);
  }
}
