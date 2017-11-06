package com.github.edgar615.direwolves.circuitbreaker;

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
   * @param circuitBreakerName 断路器名称
   * @return
   */
  CircuitBreaker get(String circuitBreakerName);

  /**
   * 创建断路器注册表
   * <p>
   * 断路器的配置：
   * fallbackOnFailure : 失败后是否开启fallbackOnFailure,默认false
   * maxFailures ：最大失败次数，默认5
   * maxRetries ： 最大重试次数，默认0
   * metricsRollingWindow：度量的窗口，单位毫秒，默认值10秒
   * notificationAddress ： 度量报告的消息地址，默认vertx.circuit-breaker
   * notificationPeriod：度量的报告周期，单位毫秒，默认2秒
   * timeout ： 操作的超时时间，单位毫秒，默认10秒
   * resetTimeout：断路器打开之后等待resetTimeout毫秒之后切换到半开状态，默认30秒
   * <p>
   * cache.expires：断路器缓存时间，单位秒，默认24小时
   * state.announce：广播地址，断路器状态变化后的会向这个地址发送广播.，默认：direwolves.circuitbreaker.announce
   *
   * @param vertx  Vertx
   * @param config 断路器的配置
   * @return
   */
  static CircuitBreakerRegistry create(Vertx vertx, JsonObject config) {
    return new CircuitBreakerRegistryImpl(vertx, new CircuitBreakerRegistryOptions(config));
  }

  static CircuitBreakerRegistry create(Vertx vertx, CircuitBreakerRegistryOptions options) {
    return new CircuitBreakerRegistryImpl(vertx, options);
  }
}
