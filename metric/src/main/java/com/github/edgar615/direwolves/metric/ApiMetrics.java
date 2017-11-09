package com.github.edgar615.direwolves.metric;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import io.vertx.core.spi.metrics.Metrics;
import io.vertx.ext.dropwizard.ThroughputTimer;

import java.util.concurrent.TimeUnit;

/**
 * Created by Edgar on 2017/3/30.
 *
 * @author Edgar  Date 2017/3/30
 */
public class ApiMetrics implements Metrics {

  private volatile static ApiMetrics instance;

  private final String baseName;

  private final Cache<String, String> cache;

  private final ThroughputTimer requests;

//  private final ThroughputMeter response;

  private final Counter response2xx;

  private final Counter response4xx;

  private final Counter response5xx;

  private MetricRegistry registry;


  private ApiMetrics(MetricRegistry registry, String baseName, int maxCacheSize) {
    this.registry = registry;
    this.baseName = baseName;
    this.cache = CacheBuilder.newBuilder()
            .maximumSize(maxCacheSize)
            .build();
    requests = MetricHelper.getOrAdd(registry,
                                     MetricRegistry.name(baseName, "api", "request"),
                                     MetricHelper.THROUGHPUT_TIMER);
    response2xx = registry.counter(MetricRegistry.name(baseName, "api", "response-2xx"));
    response4xx = registry.counter(MetricRegistry.name(baseName, "api", "response-4xx"));
    response5xx = registry.counter(MetricRegistry.name(baseName, "api", "response-5xx"));
//    response = MetricHelper.getOrAdd(registry,
//                                     MetricRegistry.name(baseName, "api", "response"),
//                                     MetricHelper.THROUGHPUT_METER);
  }


  public static ApiMetrics instance() {
    if (instance == null) {
      throw new NullPointerException("instance() is null, please call create(...) first!");
    }
    return instance;
  }

  public static ApiMetrics create(MetricRegistry registry, String baseName, int maxCacheSize) {
    if (instance == null) {
      synchronized (ApiMetrics.class) {
        instance = new ApiMetrics(registry, baseName, maxCacheSize);
      }
    }
    return instance;
  }

  public void request(String requestId, String api) {
    cache.put(requestId, api);
  }

  public void response(String requestId, int statusCode, long duration) {
    String api = cache.getIfPresent(requestId);
    cache.invalidate(requestId);
    if (api == null) {
      return;
    }
    requests.update(duration, TimeUnit.MILLISECONDS);

    ThroughputTimer apiTimer =
            MetricHelper.getOrAdd(registry,
                                  MetricRegistry.name(baseName, "api", api, "response"),
                                  MetricHelper.THROUGHPUT_TIMER);
    apiTimer.update(duration, TimeUnit.MILLISECONDS);

    if (statusCode >= 200 && statusCode < 300) {
      response2xx.inc();
      Counter apiCounter =
              registry.counter(MetricRegistry.name(baseName, "api", api, "response-2xx"));
      apiCounter.inc();
    }
    if (statusCode >= 400 && statusCode < 500) {
      response4xx.inc();
      Counter apiCounter =
              registry.counter(MetricRegistry.name(baseName, "api", api, "response-4xx"));
      apiCounter.inc();
    }

    if (statusCode >= 500) {
      response5xx.inc();
      Counter apiCounter =
              registry.counter(MetricRegistry.name(baseName, "api", api, "response-5xx"));
      apiCounter.inc();
    }
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public void close() {

  }

  public long cacheSize() {
    return cache.size();
  }

}
