package com.edgar.direwolves.plugin.response;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 16-11-5.
 */
public class ExtractValueFlter implements Filter {
  private static final String NAME = "extractvalue";

  @Override
  public String name() {
    return NAME;
  }

  @Override
  public String type() {
    return POST;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    return apiContext.apiDefinition().plugin(ExtractValuePlugin.NAME) != null
        && apiContext.response().size() == 1;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    JsonObject result = apiContext.response().getJsonObject(0);
    completeFuture.complete(apiContext);
  }

  @Override
  public void config(Vertx vertx, JsonObject config) {

  }
}
