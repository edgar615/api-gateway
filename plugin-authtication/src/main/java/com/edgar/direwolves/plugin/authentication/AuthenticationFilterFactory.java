package com.edgar.direwolves.plugin.authentication;

import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.dispatch.FilterFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 16-12-11.
 */
public class AuthenticationFilterFactory implements FilterFactory {
  @Override
  public String name() {
    return AuthenticationFilter.class.getSimpleName();
  }

  @Override
  public Filter create(Vertx vertx, JsonObject config) {
    return new AuthenticationFilter(vertx, config);
  }
}