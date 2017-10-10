package com.github.edgar615.direwolves.plugin.appkey;

import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.direwolves.core.dispatch.FilterFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 16-12-22.
 */
public class AppKeyFilterFactory implements FilterFactory {
  @Override
  public String name() {
    return AppKeyFilter.class.getSimpleName();
  }

  @Override
  public Filter create(Vertx vertx, JsonObject config) {
    return new AppKeyFilter(vertx, config);
  }
}
