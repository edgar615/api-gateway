package com.github.edgar615.gateway.metric;

import com.github.edgar615.gateway.core.metric.ApiMetric;
import org.junit.Test;

import java.util.UUID;

/**
 * Created by Edgar on 2017/4/1.
 *
 * @author Edgar  Date 2017/4/1
 */
public class ApiMetricTest {

  @Test
  public void testRequest() {
    String id = UUID.randomUUID().toString();
    String api = "test_api";
    ApiMetric.request(id, api);

    ApiMetric.response(id,  api, 200, 1);
  }
}
