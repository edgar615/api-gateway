package com.github.edgar615.direvolves.plugin.auth;

import com.google.common.base.Splitter;

import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.direwolves.core.utils.Consts;
import com.github.edgar615.direwolves.core.utils.Log;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
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

    String appKey = (String) apiContext.variables().get("client_appKey");
    permissionCheck(appKey, permission, ar -> {
      if (ar.succeeded()) {
        Log.create(LOGGER)
                .setTraceId(apiContext.id())
                .setEvent("ClientPermissionAdmitted")
                .info();
        completeFuture.complete(apiContext);
      } else {
        SystemException ex = SystemException.create(DefaultErrorCode.PERMISSION_DENIED)
                .setDetails( "The appKey does not have permission");
        failed(completeFuture, apiContext.id(), "AppKeyPermissionDenied", ex);
      }
    });

  }

  /**
   * 调用认证服务验证调用方权限
   *
   * @param appKey
   * @param permission
   * @return
   */
  private void permissionCheck(String appKey, String permission,
                               Handler<AsyncResult<Boolean>> completeHandler) {
    vertx.createHttpClient().get(port, "127.0.0.1", path, response -> {
      if (response.statusCode() >= 400) {
        completeHandler.handle(Future.succeededFuture(false));
      } else {
        completeHandler.handle(Future.succeededFuture(true));
      }
    }).exceptionHandler(e ->  completeHandler.handle(Future.succeededFuture(false)))
            .end(new JsonObject().put("appKey", appKey).put("permission", permission).encode());
  }
}