package com.github.edgar615.gateway.plugin.predicate;

import com.github.edgar615.gateway.core.definition.ApiDefinition;
import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.gateway.core.dispatch.Filter;
import io.vertx.core.Future;

public class ApiPredicateFilter implements Filter {
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
    return false;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {

  }
}
