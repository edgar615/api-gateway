package com.github.edgar615.direwolves.plugin.version;

import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.direwolves.core.dispatch.FilterFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class VersionSplitterFilterFactory implements FilterFactory {
  @Override
  public String name() {
    return VersionSplitterFilter.class.getSimpleName();
  }

  @Override
  public Filter create(Vertx vertx, JsonObject config) {
    return new VersionSplitterFilter(vertx, config);
  }
}
