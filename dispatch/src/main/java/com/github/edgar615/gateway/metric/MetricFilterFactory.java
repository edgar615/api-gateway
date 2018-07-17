package com.github.edgar615.gateway.metric;

import com.github.edgar615.gateway.core.dispatch.Filter;
import com.github.edgar615.gateway.core.dispatch.FilterFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/11/10.
 *
 * @author Edgar  Date 2017/11/10
 */
public class MetricFilterFactory implements FilterFactory {
  @Override
  public String name() {
    return MetricFilter.class.getSimpleName();
  }

  @Override
  public Filter create(Vertx vertx, JsonObject config) {
    return new MetricFilter(vertx, config);
  }
}
