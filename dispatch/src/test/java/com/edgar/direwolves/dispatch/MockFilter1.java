package com.edgar.direwolves.dispatch;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class MockFilter1 implements Filter {

  private static final String NAME = "MockFilter1";

  private Vertx vertx;

  @Override
  public String type() {
    return PRE;
  }

  @Override
  public int order() {
    return 0;
  }

  @Override
  public void config(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    return true;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {

    if (!apiContext.variables().containsKey("mock1")) {
      throw new NullPointerException();
    }
    Boolean test = (Boolean) apiContext.variables().get("mock1");
    if (test) {
      completeFuture.complete(apiContext);
    } else {
      completeFuture.fail("mock1 unkown");
    }
  }

}
