package com.edgar.direwolves.metric;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import io.vertx.core.spi.metrics.Metrics;
import io.vertx.ext.dropwizard.ThroughputTimer;

import java.util.function.Function;

/**
 * Created by Edgar on 2017/3/30.
 *
 * @author Edgar  Date 2017/3/30
 */
public class ApiMetrics implements Metrics {
  private static final Function<Metric, ThroughputTimer> THROUGHPUT_TIMER = metric -> {
    if (metric != null) {
      return (ThroughputTimer) metric;
    } else {
      return new ThroughputTimer();
    }
  };

  private final Counter requests;

  private MetricRegistry registry;

  private String baseName;

  public ApiMetrics(MetricRegistry registry, String baseName) {
    this.registry = registry;
    this.baseName = baseName;
    requests = registry.counter(MetricRegistry.name(baseName, "api"));
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
