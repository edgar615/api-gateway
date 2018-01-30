package com.github.edgar615.direwolves.plugin.scope;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;

import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import com.github.edgar615.util.log.Log;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * 权限校验的filter.
 * <p>
 * 如果接口包括AppKeyPlugin插件，那么在AppKeyFilter调用之后会在上下文中存入<b>app.permissions</b>变量
 * 如果调用方或者用户没有对应的权限，直接返回1004的错误.
 * <p>
 * <p>
 * 该filter的order=1100
 */
public class AppKeyScopeFilter implements Filter {
  private static final Logger LOGGER = LoggerFactory.getLogger(AppKeyScopeFilter.class);

  AppKeyScopeFilter(Vertx vertx, JsonObject config) {
  }

  @Override
  public String type() {
    return PRE;
  }

  @Override
  public int order() {
    return 11000;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    return apiContext.apiDefinition().plugin(ScopePlugin.class.getSimpleName()) != null
           && apiContext.variables().containsKey("client.permissions");
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    ScopePlugin plugin = (ScopePlugin) apiContext.apiDefinition()
            .plugin(ScopePlugin.class.getSimpleName());
    String appScope = plugin.scope();

    Set<String> permissions = new HashSet<>();
    Object clientPermissions = apiContext.variables().get("client.permissions");
    if (clientPermissions instanceof String) {
      permissions.addAll(Splitter.on(",")
                                 .omitEmptyStrings().trimResults()
                                 .splitToList((String) clientPermissions));
    }
    if (clientPermissions instanceof JsonArray) {
      JsonArray jsonArray = (JsonArray) clientPermissions;
      jsonArray.forEach(o -> {
        if (o instanceof String ) {
          permissions.add((String) o);
        }
      });
    }

    if (permissions.contains("all") || permissions.contains(appScope)) {
      completeFuture.complete(apiContext);
    } else {
      Log.create(LOGGER)
              .setTraceId(apiContext.id())
              .setEvent("scope.tripped")
              .setMessage("AppKey does not have permission")
              .warn();
      SystemException ex = SystemException.create(DefaultErrorCode.PERMISSION_DENIED)
              .set("details", "AppKey does not have permission");
      completeFuture.fail(ex);
    }

  }

}