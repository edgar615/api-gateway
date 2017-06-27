package com.edgar.direwolves.filter;

import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.dispatch.FilterFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 16-12-27.
 */
public class RpcFilterFactory implements FilterFactory {
  @Override
  public String name() {
    return RpcFilter.class.getSimpleName();
  }

  @Override
  public Filter create(Vertx vertx, JsonObject config) {
    return new RpcFilter(vertx, config);
  }
}
