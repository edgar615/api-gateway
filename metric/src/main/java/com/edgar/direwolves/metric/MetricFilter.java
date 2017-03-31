package com.edgar.direwolves.metric;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/2/9.
 *
 * @author Edgar  Date 2017/2/9
 */
public class MetricFilter implements Filter {

  private ApiMetrics metrics;

  MetricFilter(Vertx vertx, JsonObject config) {
    MetricRegistry registry = SharedMetricRegistries.getOrCreate("my-registry");
    metrics = new ApiMetrics(registry, config.getString("project.namespace", ""));
  }

  @Override
  public String type() {
    return PRE;
  }

  @Override
  public int order() {
    return Integer.MIN_VALUE + 1;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    return true;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    metrics.request(apiContext.apiDefinition().name());
    completeFuture.complete(apiContext);
  }
}
