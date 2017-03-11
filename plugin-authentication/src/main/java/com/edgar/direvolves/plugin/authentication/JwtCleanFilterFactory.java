package com.edgar.direvolves.plugin.authentication;

import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.dispatch.FilterFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * JwtCleanFilter的工厂类.
 * Created by edgar on 16-12-11.
 */
public class JwtCleanFilterFactory implements FilterFactory {
  @Override
  public String name() {
    return JwtCleanFilter.class.getSimpleName();
  }

  @Override
  public Filter create(Vertx vertx, JsonObject config) {
    return new JwtCleanFilter(vertx, config);
  }
}