package com.edgar.direwolves.core.dispatch;

import com.edgar.util.validation.Rule;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class MockFilter implements Filter {

  private static final String NAME = "timeout";

  private final Multimap<String, Rule> commonParamRule = ArrayListMultimap.create();

  private Vertx vertx;

  private int timeout = 5 * 60;

  private JsonArray secrets = new JsonArray();

  public MockFilter() {
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
  public boolean shouldFilter(ApiContext apiContext) {
    return true;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {

    if (!apiContext.variables().containsKey("test")) {
      throw new NullPointerException();
    }
    Boolean test = (Boolean) apiContext.variables().get("test");
    if (test) {
      completeFuture.complete(apiContext);
    } else {
      completeFuture.fail("unkown");
    }
  }

}
