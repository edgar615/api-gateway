package com.edgar.direwolves.plugin.authentication;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.AuthenticationStrategy;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Basic类型token的校验.
 * 在校验通过之后，会设置上下文的principal.
 * <p/>
 * 如果开启了这个过滤器，那么对API的调用必须包含请求头Authorization: Basic <token>，如果不包含该格式的请求头，服务端会认为是非法请求。
 * <p/>
 * Created by edgar on 16-9-20.
 */
@Deprecated //尚未实现只是一个简单的版本
public class BasicStrategy implements AuthenticationStrategy {
  private static final String HEADER_AUTH = "Authorization";

  private static final String AUTH_PREFIX = "Basic ";

  private static final String NAME = "basic";

  private Vertx vertx;

  @Override
  public void config(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
  }

  @Override
  public void doAuthentication(ApiContext apiContext, Future<JsonObject> completeFuture) {
    try {
      String token = extractToken(apiContext);
      auth(token, apiContext, completeFuture);
    } catch (Exception e) {
      completeFuture.fail(e);
    }
  }

  @Override
  public String name() {
    return NAME;
  }

  /**
   * 从header中提取token信息.
   *
   * @param apiContext
   */
  private String extractToken(ApiContext apiContext) {
    if (apiContext.headers().containsKey(HEADER_AUTH)) {
      List<String> authorizationHeaders = new ArrayList<>(apiContext.headers().get(HEADER_AUTH));
      String authorization = authorizationHeaders.get(0);
      if (!Strings.isNullOrEmpty(authorization) && authorization.startsWith(AUTH_PREFIX)) {
        return authorization.substring(AUTH_PREFIX.length());
      }
    }
    throw SystemException.create(DefaultErrorCode.INVALID_TOKEN);
  }

  private void auth(String token, ApiContext apiContext, Future<JsonObject> completeFuture) {
    List<String> list = null;
    try {
      String decode = new String(Base64.getDecoder().decode(token));
      list = Lists
          .newArrayList(Splitter.on(":").omitEmptyStrings().trimResults().split(decode));
    } catch (Exception e) {
      completeFuture.fail(SystemException.create(DefaultErrorCode.INVALID_TOKEN));
      return;
    }
    if (list.size() != 2) {
      completeFuture.fail(SystemException.create(DefaultErrorCode.INVALID_TOKEN));
      return;
    }
    String username = list.get(0);
    String password = list.get(1);
    if ("edgar".equalsIgnoreCase(username) && "123".equals(password)) {
      JsonObject principal = new JsonObject()
          .put("username", "edgar")
          .put("role", "super");
      apiContext.setPrincipal(principal);
      completeFuture.complete(principal);
    } else {
      completeFuture.fail(SystemException.create(DefaultErrorCode.NO_AUTHORITY));
    }
  }

}
