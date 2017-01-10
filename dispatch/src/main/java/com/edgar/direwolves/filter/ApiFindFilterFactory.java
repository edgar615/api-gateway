package com.edgar.direwolves.filter;

import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.dispatch.FilterFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * ApiFindFilter的工厂类.
 * Created by edgar on 16-12-27.
 */
public class ApiFindFilterFactory implements FilterFactory {
  @Override
  public String name() {
    return ApiFindFilter.class.getSimpleName();
  }

  @Override
  public Filter create(Vertx vertx, JsonObject config) {
    return new ApiFindFilter(vertx, config);
  }
}
