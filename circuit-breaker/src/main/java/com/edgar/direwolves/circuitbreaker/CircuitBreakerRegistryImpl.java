package com.edgar.direwolves.circuitbreaker;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import com.edgar.util.log.Log;
import com.edgar.util.log.LogType;
import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.circuitbreaker.CircuitBreakerState;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Edgar on 2017/8/1.
 *
 * @author Edgar  Date 2017/8/1
 */
class CircuitBreakerRegistryImpl implements CircuitBreakerRegistry {

  private static final Logger LOGGER
          = LoggerFactory.getLogger(CircuitBreakerRegistry.class.getSimpleName());

  private static final long DEFAULT_CACHE_EXPIRES = 24 * 3600 * 1000;

  private final Vertx vertx;

  private final LoadingCache<String, CircuitBreaker> cache;

  private final CircuitBreakerOptions options;

  private String announce = "direwolves.circuitbreaker.announce";

  CircuitBreakerRegistryImpl(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    this.options = new CircuitBreakerOptions(config);
    long cacheExpires = DEFAULT_CACHE_EXPIRES;
    if (config.getValue("cache.expires") instanceof Number) {
      cacheExpires = ((Number) config.getValue("cache.expires")).longValue();
    }
    if (config.getValue("circuitbreaker.announce") instanceof String) {
      announce = (String) config.getValue("circuitbreaker.announce");
    }
    this.cache = CacheBuilder.newBuilder()
            .expireAfterAccess(cacheExpires, TimeUnit.MILLISECONDS)
            .removalListener(new RemovalListener<String, CircuitBreaker>() {
              @Override
              public void onRemoval(RemovalNotification<String, CircuitBreaker> notification) {
                Log.create(LOGGER)
                        .setLogType(LogType.LOG)
                        .setModule("CircuitBreaker")
                        .setEvent("cache.removed")
                        .addData("key", notification.getKey())
                        .setMessage("cause by: {}")
                        .addArg(notification.getCause())
                        .info();
              }
            })
            .build(new CacheLoader<String, CircuitBreaker>() {
              @Override
              public CircuitBreaker load(String circuitBreakerName) throws Exception {
                return create(circuitBreakerName);
              }
            });
  }

  @Override
  public CircuitBreaker get(String circuitBreakerName) {
    try {
      return cache.get(circuitBreakerName);
    } catch (ExecutionException e) {
      CircuitBreaker circuitBreaker = create(circuitBreakerName);
      cache.asMap().putIfAbsent(circuitBreakerName, circuitBreaker);
      return cache.asMap().get(circuitBreakerName);
    }
  }

  private CircuitBreaker create(String circuitBreakerName) {
    CircuitBreaker circuitBreaker
            = CircuitBreaker.create(circuitBreakerName, vertx, new CircuitBreakerOptions(options));
    circuitBreaker.openHandler(v -> {
      onOpen(circuitBreakerName);
    }).closeHandler(v -> {
      onClose(circuitBreakerName);
    }).halfOpenHandler(v -> {
      onHalfOpen(circuitBreakerName);
    });
    return circuitBreaker;
  }

  private void onHalfOpen(String circuitBreakerName) {
    Log.create(LOGGER)
            .setLogType(LogType.LOG)
            .setModule("CircuitBreaker")
            .setEvent("breaker.reseted")
            .addData("name", circuitBreakerName)
            .info();
    vertx.eventBus().publish(announce, new JsonObject()
            .put("name", circuitBreakerName)
            .put("state", "halfOpen"));
  }

  private void onClose(String circuitBreakerName) {
    Log.create(LOGGER)
            .setLogType(LogType.LOG)
            .setModule("CircuitBreaker")
            .setEvent("breaker.closed")
            .addData("name", circuitBreakerName)
            .info();
    vertx.eventBus().publish(announce, new JsonObject()
            .put("name", circuitBreakerName)
            .put("state", "close"));
  }

  private void onOpen(String circuitBreakerName) {
    Log.create(LOGGER)
            .setLogType(LogType.LOG)
            .setModule("CircuitBreaker")
            .setEvent("breaker.tripped")
            .addData("name", circuitBreakerName)
            .warn();
    vertx.eventBus().publish(announce, new JsonObject()
            .put("name", circuitBreakerName)
            .put("state", "open"));
  }
}
