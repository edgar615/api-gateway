package com.edgar.direwolves.core.dispatch;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

public class MockFilter1 implements Filter {

  @Override
  public String type() {
    return PRE;
  }

  @Override
  public int order() {
    return 100;
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