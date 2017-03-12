package com.edgar.direvolves.plugin.authentication;

import com.google.common.base.Strings;

import com.edgar.direwolves.core.cache.RedisProvider;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.serviceproxy.ProxyHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 身份认证.
 * 目前仅支持JWT类型的认证
 * 在校验通过之后，会在上下文中存入用户信息:
 * * 该filter可以接受下列的配置参数
 * <pre>
 *   project.namespace 项目的命名空间，用来避免多个项目冲突，默认值""
 *   keystore.path 证书的路径，默认值keystore.jceks
 *   keystore.type 证书的类型，默认值jceks，可选值：JKS, JCEKS, PKCS12, BKS，UBER
 *   keystore.password 证书的密码，默认值secret
 *   jwt.userClaimKey token中的用户主键，默认值userId
 *   jwt.user.unique 每个用户的token是否必须唯一，默认值false
 * </pre>
 * 该filter的order=1000
 *
 * @author Edgar  Date 2016/10/31
 */
public class AuthenticationFilter implements Filter {
  private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationFilter.class);

  private static final String HEADER_AUTH = "Authorization";

  private static final String AUTH_PREFIX = "Bearer ";

  private final String userKey;

  private final boolean uniqueToken;

  private final String namespace;

  private final RedisProvider redisProvider;

  private final Vertx vertx;

  private JsonObject jwtConfig = new JsonObject()
          .put("path", "keystore.jceks")
          .put("type", "jceks")//JKS, JCEKS, PKCS12, BKS，UBER
          .put("password", "secret");

  AuthenticationFilter(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    if (config.containsKey("keystore.path")) {
      this.jwtConfig.put("path", config.getString("keystore.path"));
    }
    if (config.containsKey("keystore.type")) {
      this.jwtConfig.put("type", config.getString("keystore.type"));
    }
    if (config.containsKey("keystore.password")) {
      this.jwtConfig.put("password", config.getString("keystore.password"));
    }

    this.userKey = config.getString("jwt.userClaimKey", "userId");
    this.namespace = config.getString("project.namespace", "");
    this.uniqueToken = config.getBoolean("jwt.user.unique", false);
    String address = RedisProvider.class.getName();
    if (!Strings.isNullOrEmpty(namespace)) {
      address = namespace + "." + address;
    }
    this.redisProvider = ProxyHelper.createProxy(RedisProvider.class, vertx, address);
  }

  @Override
  public String type() {
    return PRE;
  }

  @Override
  public int order() {
    return 1000;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    return apiContext.apiDefinition()
                   .plugin(AuthenticationPlugin.class.getSimpleName()) != null;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    try {
      String token = extractToken(apiContext);
      Future<JsonObject> authFuture = auth(token);
      authFuture.compose(this::userCheck)
              .setHandler(ar -> {
                if (ar.succeeded()) {
                  JsonObject principal = ar.result();
                  apiContext.setPrincipal(principal);
                  completeFuture.complete(apiContext);
                } else {
                  completeFuture.fail(ar.cause());
                }
              });
    } catch (Exception e) {
      completeFuture.fail(e);
    }
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
    throw SystemException.create(DefaultErrorCode.INVALID_TOKEN)
            .set("details", "Request header: Authorization is undefined");
  }

  private Future<JsonObject> userCheck(JsonObject principal) {
    Future<JsonObject> userFuture = Future.future();
    String clientJti = principal.getString("jti");
    Integer userId = principal.getInteger(userKey);
    if (userId == null) {
      LOGGER.debug("jwt failed, error->userId not found");
      userFuture.fail(SystemException.create(DefaultErrorCode.INVALID_TOKEN)
                              .set("details", "Token must contain " + userKey));
    } else {
      String userCacheKey = namespace + ":user:" + userId;
      redisProvider.get(userCacheKey, ar -> {
        if (ar.succeeded()) {
          if (uniqueToken) {
            String serverJti = ar.result().getString("jti", UUID.randomUUID().toString());
            if (serverJti.equalsIgnoreCase(clientJti)) {
              userFuture.complete(ar.result());
            } else {
              userFuture.fail(SystemException.create(DefaultErrorCode.EXPIRE_TOKEN)
                                      .set("details", "The token has been kicked out"));
            }
          } else {
            userFuture.complete(ar.result());
          }

        } else {
          userFuture.fail(SystemException.create(DefaultErrorCode.INVALID_TOKEN)
                                  .set("details", "User not found"));
        }
      });
    }
    return userFuture;
  }

  private Future<JsonObject> auth(String token) {
    Future<JsonObject> authFuture = Future.future();
    JsonObject jwtConfig = new JsonObject().put("keyStore", this.jwtConfig);

    JWTAuth provider = JWTAuth.create(vertx, jwtConfig);
    provider.authenticate(new JsonObject().put("jwt", token), ar -> {
      if (ar.succeeded()) {
        JsonObject principal = ar.result().principal();
        LOGGER.debug("jwt succeed");
        authFuture.complete(principal);
      } else {
        LOGGER.debug("jwt failed, error->{}", ar.cause());
        fail(authFuture, ar);
      }
    });
    return authFuture;
  }

  private void fail(Future<JsonObject> completeFuture, AsyncResult<User> ar) {
    String errorMessage = ar.cause().getMessage();
    if (errorMessage != null) {
      if (errorMessage.startsWith("Expired JWT token")) {
        completeFuture.fail(SystemException.wrap(DefaultErrorCode.EXPIRE_TOKEN, ar.cause())
                                    .set("details", "Expired JWT token"));
      } else if (errorMessage.startsWith("Invalid JWT token")) {
        completeFuture.fail(SystemException.wrap(DefaultErrorCode.INVALID_TOKEN, ar.cause())
                                    .set("details", "Invalid JWT token"));
      } else {
        completeFuture.fail(SystemException.wrap(DefaultErrorCode.NO_AUTHORITY, ar.cause())
                                    .set("details", ar.cause().getMessage()));
      }
    } else {
      completeFuture.fail(SystemException.wrap(DefaultErrorCode.NO_AUTHORITY, ar.cause())
                                  .set("details", ar.cause().getMessage()));
    }
  }

}
