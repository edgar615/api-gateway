package com.github.edgar615.gateway.filter;

import com.github.edgar615.gateway.core.dispatch.Filter;
import com.github.edgar615.gateway.core.dispatch.FilterFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 16-12-27.
 */
public class SimpleHttpRequestFilterFactory implements FilterFactory {
  @Override
  public String name() {
    return SimpleHttpRequestFilter.class.getSimpleName();
  }

  @Override
  public Filter create(Vertx vertx, JsonObject config) {
    return new SimpleHttpRequestFilter();
  }
}
