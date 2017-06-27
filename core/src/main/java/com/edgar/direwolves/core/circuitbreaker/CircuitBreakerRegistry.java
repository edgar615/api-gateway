package com.edgar.direwolves.core.circuitbreaker;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.Shareable;

/**
 * Created by Edgar on 2017/6/27.
 *
 * @author Edgar  Date 2017/6/27
 */
public class CircuitBreakerRegistry implements Shareable {

  private final Vertx vertx;

  private final CircuitBreaker breaker;

  public CircuitBreakerRegistry(Vertx vertx, String name) {
    this.vertx = vertx;
    breaker = CircuitBreaker.create(name, vertx, new CircuitBreakerOptions())
            .openHandler(v -> {
              System.out.println("Circuit opened");
            }).closeHandler(v -> {
              System.out.println("Circuit closed");
            }).halfOpenHandler(v -> {
              System.out.println("reset (half-open state)");
            });
  }

  public CircuitBreaker get() {
    return breaker;
  }
}
