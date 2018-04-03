package com.github.edgar615.direwolves.circuitbreaker;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.core.Vertx;

/**
 * 断路器的注册表.
 *
 * @author Edgar  Date 2017/8/1
 */
public interface CircuitBreakerRegistry {

  /**
   * 获取服务节点对应的断路器
   *
   * @param circuitBreakerName 断路器名称
   * @return
   */
  CircuitBreaker get(String circuitBreakerName);

  static CircuitBreakerRegistry create(Vertx vertx, CircuitBreakerRegistryOptions options) {
    return new CircuitBreakerRegistryImpl(vertx, options);
  }
}
