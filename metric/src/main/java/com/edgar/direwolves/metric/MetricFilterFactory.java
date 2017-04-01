package com.edgar.direwolves.metric;

import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.dispatch.FilterFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/3/31.
 *
 * @author Edgar  Date 2017/3/31
 */
@Deprecated
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
