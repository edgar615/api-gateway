package com.github.edgar615.direwolves.metric;

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
public class ApiMetric implements Metrics {

  public static void request(String baseName, String api) {
    MetricRegistry registry = MetricHelper.registry();
    Counter request = registry.counter(MetricRegistry.name(baseName, "api", "request"));
    request.inc();
    Counter apiRequest = registry.counter(MetricRegistry.name(baseName, "api", api, "request"));
    apiRequest.inc();
  }

  public static void response(String baseName,String api, int statusCode, long duration) {
    MetricRegistry registry = MetricHelper.registry();
    //统计全局响应时间
    ThroughputTimer requestThroughput = MetricHelper.getOrAdd(registry,
                                                              MetricRegistry.name(baseName, "api",
                                                                                  "response"),
                                                              MetricHelper.THROUGHPUT_TIMER);
    requestThroughput.update(duration, TimeUnit.MILLISECONDS);

    //单个API的响应时间
    ThroughputTimer apiTimer =
            MetricHelper.getOrAdd(registry,
                                  MetricRegistry.name(baseName, "api", api, "response"),
                                  MetricHelper.THROUGHPUT_TIMER);
    apiTimer.update(duration, TimeUnit.MILLISECONDS);

    //根据状态统计
    if (statusCode >= 200 && statusCode < 300) {
      Counter response2xx =
              registry.counter(MetricRegistry.name(baseName, "api", "response-2xx"));
      response2xx.inc();
      Counter apiCounter =
              registry.counter(MetricRegistry.name(baseName, "api", api, "response-2xx"));
      apiCounter.inc();
    }
    if (statusCode >= 400 && statusCode < 500) {
      Counter response4xx =
              registry.counter(MetricRegistry.name(baseName, "api", "response-4xx"));
      response4xx.inc();
      Counter apiCounter =
              registry.counter(MetricRegistry.name(baseName, "api", api, "response-4xx"));
      apiCounter.inc();
    }

    if (statusCode >= 500) {
      Counter response5xx =
              registry.counter(MetricRegistry.name(baseName, "api", "response-5xx"));
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

}
