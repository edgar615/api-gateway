package com.edgar.direwolves.loadbalance;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * 断路器的注册表.
 *
 * @author Edgar  Date 2017/8/1
 */
public interface CircuitBreakerRegistry {

  /**
   * 获取服务节点对应的断路器
   *
   * @param serviceId 节点ID
   * @return
   */
  CircuitBreaker get(String serviceId);

  /**
   * 创建断路器注册表
   *
   * @param vertx  Vertx
   * @param config 断路器的配置
   * @return
   */
  static CircuitBreakerRegistry create(Vertx vertx, JsonObject config) {
    return new CircuitBreakerRegistryImpl(vertx, config);
  }
}
