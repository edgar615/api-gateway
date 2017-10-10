package com.github.edgar615.direwolves.filter;

import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.direwolves.core.dispatch.FilterFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 16-12-27.
 */
public class EventbusRequestFilterFactory implements FilterFactory {
  @Override
  public String name() {
    return EventBusRequestFilter.class.getSimpleName();
  }

  @Override
  public Filter create(Vertx vertx, JsonObject config) {
    return new EventBusRequestFilter();
  }
}
