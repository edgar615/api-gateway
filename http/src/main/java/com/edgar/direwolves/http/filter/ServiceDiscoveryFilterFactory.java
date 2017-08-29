package com.edgar.direwolves.http.filter;

import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.dispatch.FilterFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 16-12-27.
 */
@Deprecated
public class ServiceDiscoveryFilterFactory implements FilterFactory {
  @Override
  public String name() {
    return ServiceDiscoveryFilter.class.getSimpleName();
  }

  @Override
  public Filter create(Vertx vertx, JsonObject config) {
    return new ServiceDiscoveryFilter(vertx, config);
  }
}
