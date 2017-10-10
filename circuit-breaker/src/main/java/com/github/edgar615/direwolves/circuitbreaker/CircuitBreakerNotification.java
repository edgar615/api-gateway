package com.github.edgar615.direwolves.circuitbreaker;

import io.vertx.circuitbreaker.CircuitBreakerState;

/**
 * Created by Edgar on 2017/8/25.
 *
 * @author Edgar  Date 2017/8/25
 */
@Deprecated
public class CircuitBreakerNotification {

  private final CircuitBreakerState state;

  private final String name;

  public CircuitBreakerNotification(CircuitBreakerState state, String name) {
    this.state = state;
    this.name = name;
  }

  public CircuitBreakerState state() {
    return state;
  }

  public String name() {
    return name;
  }
}
