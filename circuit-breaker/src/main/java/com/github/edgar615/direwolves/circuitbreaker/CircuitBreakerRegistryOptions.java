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

  private static final String DEFAULT_ANNOUNCE = "direwolves.circuitbreaker.announce";

  private String announce = DEFAULT_ANNOUNCE;

  private long cacheExpires = DEFAULT_CACHE_EXPIRES;

  public CircuitBreakerRegistryOptions(JsonObject json) {
    super(json);
    if (json.getValue("cache.expires") instanceof Number) {
      cacheExpires = ((Number) json.getValue("cache.expires")).longValue();
    }
    if (json.getValue("state.announce") instanceof String) {
      announce = (String) json.getValue("state.announce");
    }
  }

  @Override
  public JsonObject toJson() {
    JsonObject jsonObject = super.toJson();
    jsonObject.put("cache.expires", cacheExpires);
    jsonObject.put("state.announce", announce);
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
