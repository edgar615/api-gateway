package com.edgar.direwolves.metric;

import com.codahale.metrics.MetricRegistry;
import com.hazelcast.internal.metrics.Metric;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

/**
 * Created by Edgar on 2017/4/1.
 *
 * @author Edgar  Date 2017/4/1
 */
public class ApiMetricTest {

  @Test
  public void testSingleton() {
    MetricRegistry registry = new MetricRegistry();
    ApiMetrics apiMetrics = ApiMetrics.create(registry, "test", 100);
    ApiMetrics apiMetrics1 = ApiMetrics.instance();
    ApiMetrics apiMetrics2 = ApiMetrics.instance();

    Assert.assertSame(apiMetrics1, apiMetrics);
    Assert.assertSame(apiMetrics1, apiMetrics2);
  }

  @Test
  public void testRequest() {
    MetricRegistry registry = new MetricRegistry();
    ApiMetrics apiMetrics = ApiMetrics.create(registry, "test", 100);
    String id = UUID.randomUUID().toString();
    String api = "test_api";
    apiMetrics.request(id, api);
    Assert.assertEquals(1, apiMetrics.cacheSize());

    apiMetrics.response(id, 200, 1);
    Assert.assertEquals(0, apiMetrics.cacheSize());
  }
}
