package com.github.edgar615.direwolves.circuitbreaker;

import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/11/6.
 *
 * @author Edgar  Date 2017/11/6
 */
public class CircuitBreakerRegistryOptions extends CircuitBreakerOptions {
  private static final long DEFAULT_CACHE_EXPIRES = 24 * 3600;

  private static final String DEFAULT_ANNOUNCE = "circuitbreaker.announce";

  private String announce = DEFAULT_ANNOUNCE;

  private long cacheExpires = DEFAULT_CACHE_EXPIRES;

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
   * cacheExpires：断路器缓存时间，单位秒，默认24小时
   * stateAnnounce：广播地址，断路器状态变化后的会向这个地址发送广播.，默认：circuitbreaker.announce
   *
   * @param json 断路器的配置
   * @return
   */
  public CircuitBreakerRegistryOptions(JsonObject json) {
    super(json);
    if (json.getValue("cacheExpires") instanceof Number) {
      cacheExpires = ((Number) json.getValue("cacheExpires")).longValue();
    }
  }

  @Override
  public JsonObject toJson() {
    JsonObject jsonObject = super.toJson();
    jsonObject.put("cacheExpires", cacheExpires);
    return jsonObject;
  }

  public String getAnnounce() {
    return announce;
  }

  public CircuitBreakerRegistryOptions setAnnounce(String announce) {
    this.announce = announce;
    return this;
  }

  public long getCacheExpires() {
    return cacheExpires;
  }

  public CircuitBreakerRegistryOptions setCacheExpires(long cacheExpires) {
    this.cacheExpires = cacheExpires;
    return this;
  }

  @Override
  public CircuitBreakerRegistryOptions setMaxFailures(int maxFailures) {
    super.setMaxFailures(maxFailures);
    return this;
  }

  @Override
  public CircuitBreakerRegistryOptions setTimeout(long timeoutInMs) {
    super.setTimeout(timeoutInMs);
    return this;
  }

  @Override
  public CircuitBreakerRegistryOptions setFallbackOnFailure(boolean fallbackOnFailure) {
    super.setFallbackOnFailure(fallbackOnFailure);
    return this;
  }

  @Override
  public CircuitBreakerRegistryOptions setResetTimeout(long resetTimeout) {
    super.setResetTimeout(resetTimeout);
    return this;
  }

  @Override
  public CircuitBreakerRegistryOptions setNotificationAddress(String notificationAddress) {
    super.setNotificationAddress(notificationAddress);
    return this;
  }

  @Override
  public CircuitBreakerRegistryOptions setNotificationPeriod(long notificationPeriod) {
    super.setNotificationPeriod(notificationPeriod);
    return this;
  }

  @Override
  public CircuitBreakerRegistryOptions setMetricsRollingWindow(long metricsRollingWindow) {
    super.setMetricsRollingWindow(metricsRollingWindow);
    return this;
  }

  @Override
  public CircuitBreakerRegistryOptions setMaxRetries(int maxRetries) {
    super.setMaxRetries(maxRetries);
    return this;
  }
}
