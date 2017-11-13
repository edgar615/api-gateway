package com.github.edgar615.direwolves.core.metric;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import io.vertx.core.spi.metrics.Metrics;
import io.vertx.ext.dropwizard.ThroughputTimer;

import java.util.concurrent.TimeUnit;

/**
 * Created by Edgar on 2017/3/30.
 *
 * @author Edgar  Date 2017/3/30
 */
public class RpcMetric implements Metrics {

  public static void request(String baseName, String server) {
    MetricRegistry registry = MetricHelper.registry();
    Counter requestCounter =
            registry.counter(MetricRegistry.name(baseName, "endpoint", server, "request"));
    requestCounter.inc();
  }

  public static void response(String baseName, String server, int statusCode, long duration) {
    MetricRegistry registry = MetricHelper.registry();
    ThroughputTimer requestTimer =
            MetricHelper.getOrAdd(registry,
                                  MetricRegistry.name(baseName, "endpoint", server, "request"),
                                  MetricHelper.THROUGHPUT_TIMER);
    requestTimer.update(duration, TimeUnit.MILLISECONDS);

    if (statusCode >= 200 && statusCode < 300) {
      Counter apiCounter =
              registry.counter(MetricRegistry.name(baseName, "endpoint", server, "response-2xx"));
      apiCounter.inc();
    }
    if (statusCode >= 400 && statusCode < 500) {
      Counter apiCounter =
              registry.counter(MetricRegistry.name(baseName, "endpoint", server, "response-4xx"));
      apiCounter.inc();
    }

    if (statusCode >= 500) {
      Counter apiCounter =
              registry.counter(MetricRegistry.name(baseName, "endpoint", server, "response-5xx"));
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

}
