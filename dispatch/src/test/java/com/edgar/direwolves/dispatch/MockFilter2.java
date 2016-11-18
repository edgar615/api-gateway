package com.edgar.direwolves.dispatch;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class MockFilter2 implements Filter {

  private static final String NAME = "MockFilter2";

  private Vertx vertx;

  public MockFilter2() {
  }

  @Override
  public String name() {
    return NAME;
  }

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

    if (!apiContext.variables().containsKey("mock2")) {
      throw new NullPointerException();
    }
    Boolean test = (Boolean) apiContext.variables().get("mock2");
    if (test) {
      completeFuture.complete(apiContext);
    } else {
      completeFuture.fail("mock2 unkown");
    }
  }

}
