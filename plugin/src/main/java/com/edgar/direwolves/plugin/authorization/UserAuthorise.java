package com.edgar.direwolves.plugin.authorization;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.plugin.appkey.AppKeyCheckerPlugin;
import com.edgar.direwolves.plugin.authentication.AuthenticationPlugin;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.Set;

public class UserAuthorise implements Filter {

  private final Vertx vertx;

  private final String permissionsKey;

  UserAuthorise(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    this.permissionsKey = config.getString("jwt.permissionsClaimKey", "permissions");
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
    return apiContext.apiDefinition().plugin(AuthenticationPlugin.class.getSimpleName()) != null
        && apiContext.apiDefinition().plugin(AuthorityPlugin.class.getSimpleName()) != null;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {

    if (apiContext.principal() == null) {
      completeFuture.fail(SystemException.create(DefaultErrorCode.NO_AUTHORITY));
      return;
    }

    String userPermissions = apiContext.principal().getString(permissionsKey, "default");
    Set<String> permissions = Sets.newHashSet(Splitter.on(",").omitEmptyStrings().trimResults()
        .split(userPermissions));
    AuthorityPlugin plugin = (AuthorityPlugin) apiContext.apiDefinition().plugin(AuthorityPlugin.class.getSimpleName());
    String appScope = plugin.scope();
    if (permissions.contains(appScope)) {
      completeFuture.complete(apiContext);
    } else {
      completeFuture.fail(SystemException.create(DefaultErrorCode.NO_AUTHORITY));
    }


  }

}