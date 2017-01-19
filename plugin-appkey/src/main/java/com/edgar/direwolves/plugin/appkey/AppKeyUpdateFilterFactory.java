package com.edgar.direwolves.plugin.appkey;

import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.dispatch.FilterFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * AppKeyUpdateFilter的工厂类.
 * Created by edgar on 16-12-11.
 */
public class AppKeyUpdateFilterFactory implements FilterFactory {
  @Override
  public String name() {
    return AppKeyUpdateFilter.class.getSimpleName();
  }

  @Override
  public Filter create(Vertx vertx, JsonObject config) {
    return new AppKeyUpdateFilter(vertx, config);
  }
}