package com.github.edgar615.gateway.plugin.auth;

import com.github.edgar615.gateway.core.dispatch.Filter;
import com.github.edgar615.gateway.core.dispatch.FilterFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 16-12-22.
 */
public class AppKeyPermissionFilterFactory implements FilterFactory {
  @Override
  public String name() {
    return AppKeyPermissionFilter.class.getSimpleName();
  }

  @Override
  public Filter create(Vertx vertx, JsonObject config) {
    return new AppKeyPermissionFilter(vertx, config);
  }
}
