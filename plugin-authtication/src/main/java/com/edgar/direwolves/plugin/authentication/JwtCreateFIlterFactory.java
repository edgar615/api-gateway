package com.edgar.direwolves.plugin.authentication;

import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.dispatch.FilterFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 16-12-11.
 */
public class JwtCreateFIlterFactory implements FilterFactory {
  @Override
  public String name() {
    return JwtCreateFilter.class.getSimpleName();
  }

  @Override
  public Filter create(Vertx vertx, JsonObject config) {
    return new JwtCreateFilter(vertx, config);
  }
}
