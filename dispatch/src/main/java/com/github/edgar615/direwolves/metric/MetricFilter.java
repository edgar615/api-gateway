package com.github.edgar615.direwolves.metric;

import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.direwolves.core.metric.ApiMetric;
import com.github.edgar615.direwolves.core.utils.Consts;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * 用于记录度量值的filter.
 * 只记录系统中定义了的API，其他请求不考虑
 *
 * @author Edgar  Date 2017/11/10
 */
public class MetricFilter implements Filter {
  private final Vertx vertx;

  private final String namespace;

  public MetricFilter(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    this.namespace = config.getString("namespace", Consts.DEFAULT_NAMESPACE);
  }

  @Override
  public String type() {
    return PRE;
  }

  @Override
  public int order() {
    return Integer.MIN_VALUE + 1100;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    return apiContext.apiDefinition() != null;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    try {
      ApiMetric.request(namespace, apiContext.apiDefinition().name());
    } catch (Exception e) {
      //ignore
    }
    completeFuture.complete(apiContext);
  }
}
