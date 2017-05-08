package com.edgar.direwolves.metric;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.edgar.direwolves.core.rpc.RpcMetric;
import io.vertx.core.spi.metrics.Metrics;
import io.vertx.ext.dropwizard.ThroughputTimer;

import java.util.concurrent.TimeUnit;

/**
 * Created by Edgar on 2017/3/30.
 *
 * @author Edgar  Date 2017/3/30
 */
public class RpcMetricsImpl implements Metrics, RpcMetric {

  private volatile static RpcMetricsImpl instance;

  private final String baseName;

  private MetricRegistry registry;

  private RpcMetricsImpl(MetricRegistry registry, String baseName) {
    this.registry = registry;
    this.baseName = baseName;
  }

  public static RpcMetricsImpl instance() {
    if (instance == null) {
      throw new NullPointerException("instance() is null, please call create(...) first!");
    }
    return instance;
  }

  public static RpcMetricsImpl create(MetricRegistry registry, String baseName) {
    if (instance == null) {
      synchronized (RpcMetricsImpl.class) {
        instance = new RpcMetricsImpl(registry, baseName);
      }
    }
    return instance;
  }

  @Override
  public void request(String server) {
    Counter requestCounter =
            registry.counter(MetricRegistry.name(baseName, "endpoint", server, "request"));
    requestCounter.inc();
  }

  @Override
  public void response(String server, int result, long duration) {
    ThroughputTimer requestTimer =
            MetricHelper.getOrAdd(registry,
                                  MetricRegistry.name(baseName, "endpoint", server, "request"),
                                  MetricHelper.THROUGHPUT_TIMER);
    requestTimer.update(duration, TimeUnit.MILLISECONDS);

    if (result >= 200 && result < 300) {
      Counter apiCounter =
              registry.counter(MetricRegistry.name(baseName, "endpoint", server, "response-2xx"));
      apiCounter.inc();
    }
    if (result >= 400 && result < 500) {
      Counter apiCounter =
              registry.counter(MetricRegistry.name(baseName, "endpoint", server, "response-4xx"));
      apiCounter.inc();
    }

    if (result >= 500) {
      Counter apiCounter =
              registry.counter(MetricRegistry.name(baseName, "endpoint", server, "response-5xx"));
      apiCounter.inc();
    }
  }

  @Override
  public void failed(String server) {
    Counter counter =
            registry.counter(MetricRegistry.name(baseName, "endpoint", server, "failed"));
    counter.inc();
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public void close() {

  }

}
