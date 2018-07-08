package com.github.edgar615.direvolves.plugin.auth;

import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.direwolves.core.dispatch.FilterFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class UserHeaderFilterFactory implements FilterFactory {
  @Override
  public String name() {
    return UserHeaderFilter.class.getSimpleName();
  }

  @Override
  public Filter create(Vertx vertx, JsonObject config) {
    return new UserHeaderFilter(vertx, config);
  }
}