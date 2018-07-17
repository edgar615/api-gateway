package com.github.edgar615.gateway.plugin.user;

import com.github.edgar615.gateway.core.dispatch.Filter;
import com.github.edgar615.gateway.core.dispatch.FilterFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 16-10-28.
 */
public class UserRestrictionFilterFactory implements FilterFactory {

  @Override
  public String name() {
    return UserRestrictionFilter.class.getSimpleName();
  }

  @Override
  public Filter create(Vertx vertx, JsonObject config) {
    return new UserRestrictionFilter(config);
  }
}
