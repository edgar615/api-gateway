package com.edgar.direwolves.plugin.authorization;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.plugin.appkey.AppKeyCheckerPlugin;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.Set;

public class AppKeyAuthorise implements Filter {

  private Vertx vertx;

  AppKeyAuthorise(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
  }

  @Override
  public String type() {
    return PRE;
  }

  @Override
  public int order() {
    return 1;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    return apiContext.apiDefinition().plugin(AppKeyCheckerPlugin.class.getSimpleName()) != null
        && apiContext.apiDefinition().plugin(AuthorityPlugin.class.getSimpleName()) != null;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    String appPermissions = (String) apiContext.variables().getOrDefault("app.permissions", "default");
    Set<String> permissions = Sets.newHashSet(Splitter.on(",").omitEmptyStrings().trimResults()
        .split(appPermissions));
    AuthorityPlugin plugin = (AuthorityPlugin) apiContext.apiDefinition().plugin(AuthorityPlugin.class.getSimpleName());
    String appScope = plugin.scope();
    if (permissions.contains(appScope)) {
      completeFuture.complete(apiContext);
    } else {
      completeFuture.fail(SystemException.create(DefaultErrorCode.NO_AUTHORITY));
    }


  }

}