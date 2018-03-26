package com.github.edgar615.direwolves.plugin.version;

import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.direwolves.core.dispatch.FilterFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/11/8.
 *
 * @author Edgar  Date 2017/11/8
 */
public class VersionApiMatchFilterFactory implements FilterFactory {
  @Override
  public String name() {
    return VersionApiMatchFilter.class.getSimpleName();
  }

  @Override
  public Filter create(Vertx vertx, JsonObject config) {
    return new VersionApiMatchFilter(vertx, config);
  }
}
