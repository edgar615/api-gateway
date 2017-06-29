package com.edgar.direwolves.core.circuitbreaker;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.Shareable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Edgar on 2017/6/27.
 *
 * @author Edgar  Date 2017/6/27
 */
public class CircuitBreakerRegistry implements Shareable {

  private static final Logger LOGGER = LoggerFactory.getLogger(CircuitBreakerRegistry.class);

  private final Vertx vertx;

  private final CircuitBreaker breaker;

  public CircuitBreakerRegistry(Vertx vertx, String name) {
    this.vertx = vertx;
    breaker = CircuitBreaker.create(name, vertx, new CircuitBreakerOptions())
            .openHandler(v -> {
              LOGGER.info("BreakerTripped: {}", name);
            }).closeHandler(v -> {
              LOGGER.info("BreakerClosed: {}", name);
            }).halfOpenHandler(v -> {
              LOGGER.info("BreakerReseted: {}", name);
            });
  }

  public CircuitBreaker get() {
    return breaker;
  }
}
