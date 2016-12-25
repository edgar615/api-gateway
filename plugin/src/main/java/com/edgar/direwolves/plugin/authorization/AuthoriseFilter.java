package com.edgar.direwolves.plugin.authorization;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.Set;

public class AuthoriseFilter implements Filter {

  private Vertx vertx;

  AuthoriseFilter(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
  }

  @Override
  public String type() {
    return PRE;
  }

  @Override
  public int order() {
    return 110;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    return apiContext.apiDefinition().plugin(AuthorisePlugin.class.getSimpleName()) != null;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    AuthorisePlugin plugin = (AuthorisePlugin) apiContext.apiDefinition().plugin(AuthorisePlugin.class.getSimpleName());
    String appScope = plugin.scope();
    boolean match = true;
    if (apiContext.variables().containsKey("app.permissions")) {
      Set<String> permissions = Sets.newHashSet(Splitter.on(",").omitEmptyStrings().trimResults()
          .split((String) apiContext.variables().get("app.permissions")));
      match = permissions.contains("all") || permissions.contains(appScope);
    }

    if (apiContext.principal() != null) {
      Set<String> permissions = Sets.newHashSet(Splitter.on(",").omitEmptyStrings().trimResults()
          .split(apiContext.principal().getString("permissions", "all")));
      match = permissions.contains("all") || permissions.contains(appScope);
    }

    if (match) {
      completeFuture.complete(apiContext);
    } else {
      completeFuture.fail(SystemException.create(DefaultErrorCode.NO_AUTHORITY));
    }


  }

}