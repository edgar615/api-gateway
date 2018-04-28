package com.github.edgar615.direvolves.plugin.auth;

import com.google.common.base.Splitter;

import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.direwolves.core.utils.Consts;
import com.github.edgar615.util.log.Log;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
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
public class AppKeyPermissionFilter implements Filter {
  private static final Logger LOGGER = LoggerFactory.getLogger(AppKeyPermissionFilter.class);

  private final Vertx vertx;

  private final int port;

  private final String path;

  AppKeyPermissionFilter(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    JsonObject tokenConfig = config.getJsonObject("", new JsonObject());
    this.port = config.getInteger("port", Consts.DEFAULT_PORT);
    this.path = tokenConfig.getString("path", "/");
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
    return apiContext.apiDefinition().plugin(PermissionPlugin.class.getSimpleName()) != null
           && apiContext.variables().containsKey("client_appKey");
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    PermissionPlugin plugin = (PermissionPlugin) apiContext.apiDefinition()
            .plugin(PermissionPlugin.class.getSimpleName());
    String permission = plugin.permission();

    Set<String> permissions = new HashSet<>();
    Object clientPermissions = apiContext.variables().get("client_permissions");
    if (clientPermissions instanceof String) {
      permissions.addAll(Splitter.on(",")
                                 .omitEmptyStrings().trimResults()
                                 .splitToList((String) clientPermissions));
    }
    if (clientPermissions instanceof JsonArray) {
      JsonArray jsonArray = (JsonArray) clientPermissions;
      jsonArray.forEach(o -> {
        if (o instanceof String) {
          permissions.add((String) o);
        }
      });
    }
    if (permissions.contains("all") || permissions.contains(permission)) {
      Log.create(LOGGER)
              .setTraceId(apiContext.id())
              .setEvent("ClientPermissionAdmitted")
              .info();
      completeFuture.complete(apiContext);
    } else {
      SystemException ex = SystemException.create(DefaultErrorCode.PERMISSION_DENIED)
              .setDetails( "The appKey does not have permission")
              .set("ClientPermissions", permissions)
              .set("permission", permission);
      failed(completeFuture, apiContext.id(), "AppKeyPermissionDenied", ex);
    }

  }

}