package com.edgar.direwolves.metric;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import io.vertx.ext.dropwizard.ThroughputMeter;
import io.vertx.ext.dropwizard.ThroughputTimer;

import java.util.function.Function;

/**
 * Created by Edgar on 2017/4/1.
 *
 * @author Edgar  Date 2017/4/1
 */
public class MetricHelper {
  public static final Function<Metric, ThroughputMeter> THROUGHPUT_METER = metric -> {
    if (metric != null) {
      return (ThroughputMeter) metric;
    } else {
      return new ThroughputMeter();
    }
  };

  public static final Function<Metric, ThroughputTimer> THROUGHPUT_TIMER = metric -> {
    if (metric != null) {
      return (ThroughputTimer) metric;
    } else {
      return new ThroughputTimer();
    }
  };

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
}
