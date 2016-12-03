package com.edgar.direwolves.plugin.authentication;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.AuthenticationStrategy;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.google.common.base.Strings;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.jwt.JWTAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JWT类型token的校验.
 * 在校验通过之后，会设置上下文的principal.
 * <p/>
 * 如果开启了这个过滤器，那么对API的调用必须包含请求头Authorization: Bearer <token>，如果不包含该格式的请求头，服务端会认为是非法请求。
 * <p/>
 * 可以通过keystore.*配置项来指定jwt用的加密证书.
 * <pre>
 *   keystore.path : 证书路径，默认值keystore.jceks
 *   keystore.type : 证书类型，默认值jceks
 *   keystore.password : 证书密码，默认值secret
 * </pre>
 * <p/>
 * Created by edgar on 16-9-20.
 */
public class JwtStrategy implements AuthenticationStrategy {

  private static final Logger LOGGER = LoggerFactory.getLogger(JwtStrategy.class);

  private static final String HEADER_AUTH = "Authorization";

  private static final String AUTH_PREFIX = "Bearer ";

  private static final String NAME = "jwt";

  private String userGetAddress;

  private String userKey = "userId";

  private JsonObject config = new JsonObject()
      .put("path", "keystore.jceks")
      .put("type", "jceks")//JKS, JCEKS, PKCS12, BKS，UBER
      .put("password", "secret");

  private Vertx vertx;

  @Override
  public void config(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    if (config.containsKey("keystore.path")) {
      this.config.put("path", config.getString("keystore.path"));
    }
    if (config.containsKey("keystore.type")) {
      this.config.put("type", config.getString("keystore.type"));
    }
    if (config.containsKey("keystore.password")) {
      this.config.put("password", config.getString("keystore.password"));
    }

    this.userGetAddress = config.getString("jwt.user.get.address", "eventbus.jwt.user.get");
    this.userKey = config.getString("jwt.user.key", "userId");
  }

  @Override
  public void doAuthentication(ApiContext apiContext, Future<JsonObject> completeFuture) {
    try {
      String token = extractToken(apiContext);
      auth(token, completeFuture);
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

  private void auth(String token, Future<JsonObject> completeFuture) {

    JsonObject jwtConfig = new JsonObject().put("keyStore", config);

    JWTAuth provider = JWTAuth.create(vertx, jwtConfig);

    provider.authenticate(new JsonObject().put("jwt", token), ar -> {
      if (ar.succeeded()) {
        JsonObject principal = ar.result().principal();
        String jti = principal.getString("jti");
        Integer userId = principal.getInteger(userKey);
        if (userId == null) {
          LOGGER.debug("jwt failed, error->userId not found");
          completeFuture.fail(SystemException.create(DefaultErrorCode.INVALID_TOKEN));
        } else {
          vertx.eventBus().<JsonObject>send(userGetAddress, userId, reply -> {
            if (reply.succeeded()) {
              JsonObject user = reply.result().body();
              String userJti = user.getString("jti", UUID.randomUUID().toString());
              if (userJti.equalsIgnoreCase(jti)) {
                LOGGER.debug("jwt succeeded, userId->{}, jti->{}", userId, jti);
                completeFuture.complete(user.mergeIn(principal).copy());
              } else {
                LOGGER.debug("jwt failed, userId->{}, jti->{}, error->jti inequality", userId, jti);
                completeFuture.fail(SystemException.create(DefaultErrorCode.EXPIRE_TOKEN));
              }
            } else {
              LOGGER.debug("jwt failed,userId->{},  jti->{}, error->{}", userId, jti, reply.cause());
              completeFuture.fail(SystemException.create(DefaultErrorCode.INVALID_TOKEN));
            }
          });
        }

      } else {
        LOGGER.debug("jwt failed, error->{}", ar.cause());
        fail(completeFuture, ar);
      }
    });
  }

  private void fail(Future<JsonObject> completeFuture, AsyncResult<User> ar) {
    String errorMessage = ar.cause().getMessage();
    if (errorMessage != null) {
      if (errorMessage.startsWith("Expired JWT token")) {
        completeFuture.fail(SystemException.wrap(DefaultErrorCode.EXPIRE_TOKEN, ar.cause()));
      } else if (errorMessage.startsWith("Invalid JWT token")) {
        completeFuture.fail(SystemException.wrap(DefaultErrorCode.INVALID_TOKEN, ar.cause()));
      } else {
        completeFuture.fail(SystemException.wrap(DefaultErrorCode.NO_AUTHORITY, ar.cause()));
      }
    } else {
      completeFuture.fail(SystemException.wrap(DefaultErrorCode.NO_AUTHORITY, ar.cause()));
    }
  }
}
