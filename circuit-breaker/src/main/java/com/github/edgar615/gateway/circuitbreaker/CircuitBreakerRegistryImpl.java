package com.github.edgar615.gateway.circuitbreaker;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 断路器的注册类.
 * 内部使用个Guava Cache来实现，使用expireAfterAccess来设置过期策略。
 *
 * @author Edgar  Date 2017/8/1
 */
class CircuitBreakerRegistryImpl implements CircuitBreakerRegistry {

    private static final Logger LOGGER
            = LoggerFactory.getLogger(CircuitBreakerRegistry.class.getSimpleName());

    private final Vertx vertx;

    private final LoadingCache<String, CircuitBreaker> cache;

    private final CircuitBreakerRegistryOptions options;

    CircuitBreakerRegistryImpl(Vertx vertx, CircuitBreakerRegistryOptions options) {
        this.vertx = vertx;
        this.options = options;
        this.cache = CacheBuilder.newBuilder()
                .expireAfterAccess(options.getCacheExpires(), TimeUnit.SECONDS)
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
                = CircuitBreaker
                .create(circuitBreakerName, vertx, new CircuitBreakerOptions(options));
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
        LOGGER.info("[ApiGateway] [CircuitBreakerHalfOpen] [{}]", circuitBreakerName);
        vertx.eventBus().publish(options.getAnnounce(), new JsonObject()
                .put("name", circuitBreakerName)
                .put("state", "halfOpen"));
    }

    private void onClose(String circuitBreakerName) {
        LOGGER.info("[ApiGateway] [CircuitBreakerClose] [{}]", circuitBreakerName);
        vertx.eventBus().publish(options.getAnnounce(), new JsonObject()
                .put("name", circuitBreakerName)
                .put("state", "close"));
    }

    private void onOpen(String circuitBreakerName) {
        LOGGER.warn("[ApiGateway] [CircuitBreakerOpen] [{}]", circuitBreakerName);
        vertx.eventBus().publish(options.getAnnounce(), new JsonObject()
                .put("name", circuitBreakerName)
                .put("state", "open"));
    }
}
