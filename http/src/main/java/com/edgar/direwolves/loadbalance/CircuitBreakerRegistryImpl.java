package com.edgar.direwolves.loadbalance;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Edgar on 2017/8/1.
 *
 * @author Edgar  Date 2017/8/1
 */
class CircuitBreakerRegistryImpl implements CircuitBreakerRegistry {

  private final Vertx vertx;

  private final LoadingCache<String, CircuitBreaker> cache;

  private final CircuitBreakerOptions options;

  CircuitBreakerRegistryImpl(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    this.options = new CircuitBreakerOptions(config);
    this.cache = CacheBuilder.newBuilder()
            .expireAfterAccess(24, TimeUnit.HOURS)
            .build(new CacheLoader<String, CircuitBreaker>() {
              @Override
              public CircuitBreaker load(String serviceId) throws Exception {
                return create(serviceId);
              }
            });
  }

  @Override
  public CircuitBreaker get(String serviceId) {
    try {
      return cache.get(serviceId);
    } catch (ExecutionException e) {
      CircuitBreaker circuitBreaker = create(serviceId);
      cache.asMap().putIfAbsent(serviceId, circuitBreaker);
      return cache.asMap().get(serviceId);
    }
  }

  private CircuitBreaker create(String serviceId) {
    CircuitBreaker circuitBreaker
            = CircuitBreaker.create(serviceId, vertx, new CircuitBreakerOptions(options));
    circuitBreaker.openHandler(v -> {
      LoadBalanceStats.instance().get(serviceId).setCircuitBreakerTripped(true);
//      LOGGER.info("BreakerTripped: {}", name);
    }).closeHandler(v -> {
//      LOGGER.info("BreakerClosed: {}", name);
      LoadBalanceStats.instance().get(serviceId).setCircuitBreakerTripped(false);
    }).halfOpenHandler(v -> {
//      LOGGER.info("BreakerReseted: {}", name);
      LoadBalanceStats.instance().get(serviceId).setCircuitBreakerTripped(false);
    });
    return circuitBreaker;
  }
}
