package com.edgar.direwolves.metric;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import io.vertx.core.Vertx;
import io.vertx.ext.dropwizard.ThroughputTimer;
import io.vertx.ext.web.impl.ConcurrentLRUCache;

import java.util.function.Function;

/**
 * Created by Edgar on 2017/3/30.
 *
 * @author Edgar  Date 2017/3/30
 */
public class ApiMetricsImpl implements ApiMetrics {
  private static final Function<Metric, ThroughputTimer> THROUGHPUT_TIMER = metric -> {
    if (metric != null) {
      return (ThroughputTimer) metric;
    } else {
      return new ThroughputTimer();
    }
  };
  private volatile static ApiMetricsImpl instance;
  private final String baseName;
  private final ConcurrentLRUCache<String, String> cache;
  private final Counter requests;

  private MetricRegistry registry;

//  public ApiMetricsImpl(String registerName) {
//    MetricRegistry registry = SharedMetricRegistries.getOrCreate("my-registry");
//  }

  private ApiMetricsImpl(MetricRegistry registry, String baseName, int maxCacheSize) {
    this.registry = registry;
    this.baseName = baseName;
    this.cache = new ConcurrentLRUCache<>(maxCacheSize);
    requests = registry.counter(MetricRegistry.name(baseName, "api"));
  }

  public static ApiMetrics create(MetricRegistry registry, String baseName, int maxCacheSize) {
    if (instance == null) {
      synchronized (ApiMetricsImpl.class) {
        instance = new ApiMetricsImpl(registry, baseName, maxCacheSize);
      }
    }
    instance.setMaxCacheSize(maxCacheSize);
    return instance;
  }

  private void setMaxCacheSize(int maxCacheSize) {
    cache.setMaxSize(maxCacheSize);
  }

  public static <M extends Metric> M getOrAdd(MetricRegistry registry, String name,
                                              Function<Metric, M> metricProvider) {
    Metric metric = registry.getMetrics().get(name);
    M found = metric != null ? metricProvider.apply(metric) : null;
    if (found != null) {
      return found;
    } else if (metric == null) {
      try {
        return registry.register(name, metricProvider.apply(null));
      } catch (IllegalArgumentException e) {
        metric = registry.getMetrics().get(name);
        found = metricProvider.apply(metric);
        if (found != null) {
          return found;
        }
      }
    }
    throw new IllegalArgumentException(name + " is already used for a different type of metric");
  }

  public void request(String api) {
    requests.inc();
    Counter apiCounter = registry.counter(MetricRegistry.name(baseName, "api", "request", api));
    apiCounter.inc();
//    ThroughputTimer timer =
//            getOrAdd(registry, MetricRegistry.name(baseName, "api", "server", api),
//                     THROUGHPUT_TIMER);
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public void close() {

  }
}
