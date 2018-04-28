package com.github.edgar615.direvolves.plugin.auth;

import com.google.common.base.Strings;

import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.direwolves.core.utils.Consts;
import com.github.edgar615.util.exception.CustomErrorCode;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 身份认证.
 * 这个filter要求调用方传入token，并将token下发给认证服务器进行认证，认证通过后会将用户信息存入上下文。
 *
 * @author Edgar  Date 2018/2/5
 */
public class TokenFilter implements Filter {
  private static final String AUTH_HEADER = "Authorization";

  private static final String HEADER_PREFIX = "Bearer ";

  private final Vertx vertx;

  private final int port;

  private final String path;

  private final String headerName;

  private final String prefix;

  TokenFilter(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    JsonObject tokenConfig = config.getJsonObject("token", new JsonObject());
    this.port = config.getInteger("port", Consts.DEFAULT_PORT);
    this.path = tokenConfig.getString("path", "/");
    this.prefix = tokenConfig.getString("prefix", HEADER_PREFIX);
    this.headerName = tokenConfig.getString("headerName", AUTH_HEADER);
  }

  @Override
  public String type() {
    return PRE;
  }

  @Override
  public int order() {
    return 10000;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    return apiContext.apiDefinition()
                   .plugin(TokenPlugin.class.getSimpleName()) != null
            && apiContext.principal() == null;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    try {
      String token = extractToken(apiContext);
      auth(token, ar -> {
        if (ar.succeeded()) {
          JsonObject principal = ar.result();
          apiContext.setPrincipal(principal);
          completeFuture.complete(apiContext);
        } else {
          failed(completeFuture, apiContext.id(), "AuthFailure", ar.cause());
        }
      });
    } catch (Exception e) {
      failed(completeFuture, apiContext.id(), "AuthFailure", e);
    }
  }

  /**
   * 从header中提取token信息.
   *
   * @param apiContext
   */
  private String extractToken(ApiContext apiContext) {
    if (apiContext.headers().containsKey(headerName)) {
      List<String> authorizationHeaders = new ArrayList<>(apiContext.headers().get(headerName));
      String authorization = authorizationHeaders.get(0);
      if (!Strings.isNullOrEmpty(authorization) && authorization.startsWith(prefix)) {
        return authorization.substring(prefix.length()).trim();
      } else {
        throw SystemException.create(DefaultErrorCode.INVALID_TOKEN)
                .set("details", String.format("The format of the token: %s:%s<token>",
                                              headerName, prefix));
      }
    }
    throw SystemException.create(DefaultErrorCode.INVALID_REQ)
            .set("details", "Miss rquest header: " + headerName);
  }



  /**
   * 调用认证服务验证token
   *
   * @param token
   * @return
   */
  private void auth(String token, Handler<AsyncResult<JsonObject>> completeHandler) {
    vertx.createHttpClient().get(port, "127.0.0.1", path, response -> {
      if (response.statusCode() >= 400) {
        response.bodyHandler(body -> {
          try {
            JsonObject jsonObject = body.toJsonObject();
            int code = jsonObject.getInteger("code", 1021);
            String message = jsonObject.getString("message", "Token Invalid");
            CustomErrorCode errorCode = CustomErrorCode.create(code, message);
            SystemException e = SystemException.create(errorCode);
            completeHandler.handle(Future.failedFuture(e));
          } catch (Exception e1) {
            SystemException e = SystemException.create(DefaultErrorCode.INVALID_TOKEN);
            completeHandler.handle(Future.failedFuture(e));
          }
        });
      } else {
        response.bodyHandler(body -> {
          completeHandler.handle(Future.succeededFuture(body.toJsonObject()));
        });
      }
    }).exceptionHandler(e -> completeHandler.handle(Future.failedFuture(e)))
            .end(new JsonObject().put("token", token).encode());
  }
}
