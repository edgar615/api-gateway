package com.github.edgar615.gateway.core.metric;

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

  public static void apiRequest(String apiName) {
    request("api", apiName);
  }

  public static void apiResponse(String apiName, int statusCode, long duration) {
    response("api", apiName, statusCode, duration);
  }

  public static void serviceRequest(String service, String serviceId) {
    request(service, serviceId);
  }

  public static void serviceResponse(String service, String serviceId, int statusCode,
                                     long duration) {
    response(service, serviceId, statusCode, duration);
  }

  public static void request(String type, String name) {
    MetricRegistry registry = MetricHelper.registry();
    Counter request = registry.counter(MetricRegistry.name(type, "request"));
    request.inc();
    Counter apiRequest = registry.counter(MetricRegistry.name(type, name, "request"));
    apiRequest.inc();
  }


  public static void response(String type, String name, int statusCode, long duration) {
    MetricRegistry registry = MetricHelper.registry();
    //统计全局响应时间
    ThroughputTimer requestThroughput = MetricHelper.getOrAdd(registry,
                                                              MetricRegistry
                                                                      .name(type, "response"),
                                                              MetricHelper.THROUGHPUT_TIMER);
    requestThroughput.update(duration, TimeUnit.MILLISECONDS);

    //单个API的响应时间
    ThroughputTimer apiTimer =
            MetricHelper.getOrAdd(registry,
                                  MetricRegistry.name(type, name, "response"),
                                  MetricHelper.THROUGHPUT_TIMER);
    apiTimer.update(duration, TimeUnit.MILLISECONDS);

    //根据状态统计
    if (statusCode >= 200 && statusCode < 300) {
      Counter response2xx =
              registry.counter(MetricRegistry.name(type, "response-2xx"));
      response2xx.inc();
      Counter apiCounter =
              registry.counter(MetricRegistry.name(type, name, "response-2xx"));
      apiCounter.inc();
    }
    if (statusCode >= 400 && statusCode < 500) {
      Counter response4xx =
              registry.counter(MetricRegistry.name(type, "response-4xx"));
      response4xx.inc();
      Counter apiCounter =
              registry.counter(MetricRegistry.name(type, name, "response-4xx"));
      apiCounter.inc();
    }

    if (statusCode >= 500) {
      Counter response5xx =
              registry.counter(MetricRegistry.name(type, "response-5xx"));
      response5xx.inc();
      Counter apiCounter =
              registry.counter(MetricRegistry.name(type, name, "response-5xx"));
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
