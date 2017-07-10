package com.edgar.direvolves.plugin.authentication;

import com.google.common.base.Strings;

import com.edgar.direwolves.core.cache.RedisProvider;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.dispatch.Result;
import com.edgar.direwolves.core.utils.Log;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTOptions;
import io.vertx.serviceproxy.ProxyHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * 创建JWT类型的TOKEN.
 * 目前仅支持JWT类型的认证
 * 在校验通过之后，会在上下文中存入用户信息:
 * * 该filter可以接受下列的配置参数
 * <pre>
 *   namespace 项目的命名空间，用来避免多个项目冲突，默认值""
 *   keystore.path 证书的路径，默认值keystore.jceks
 *   keystore.type 证书的类型，默认值jceks，可选值：JKS, JCEKS, PKCS12, BKS，UBER
 *   keystore.password 证书的密码，默认值secret
 *   jwt.alg 证书的算法，默认值HS512
 *   jwt.audience string token的客户aud
 *   jwt.issuer string token的发行者iss
 *  jwt.subject string token的主题sub
 *  token.expires int token的过期时间exp，单位秒，默认值1800
 *
 *  jwt.userClaimKey token中的用户主键，默认值userId
 *   jwt.permissionKey 用户权限字段 默认值permissions
 * </pre>
 * 该filter的order=10000
 * Created by edgar on 16-11-26.
 */
public class JwtBuildFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(JwtBuildFilter.class);

  private final Vertx vertx;

  private final int expires;

  private final String userKey;

  private final RedisProvider redisProvider;

  private final String namespace;

  private final String permissionsKey;

  private final JsonObject jwtConfig = new JsonObject()
          .put("path", "keystore.jceks")
          .put("type", "jceks")//JKS, JCEKS, PKCS12, BKS，UBER
          .put("password", "secret")
          .put("algorithm", "HS512")
          .put("expiresInSeconds", 1800);

  /**
   * <pre>
   *     - keystore.path string 证书文件路径 默认值keystore.jceks
   *     - keystore.type string 证书类型，可选值 jceks, jks,默认值jceks
   *     - keystore.password string 证书密钥，默认值secret
   *     - jwt.alg string jwt的加密算法,默认值HS512
   *     - jwt.audience string token的客户aud
   *     - jwt.issuer string token的发行者iss
   *     - jwt.subject string token的主题sub
   *     - token.expires int 过期时间exp，单位秒，默认值1800
   * </pre>
   *
   * @param vertx  Vertx
   * @param config 配置
   */
  JwtBuildFilter(Vertx vertx, JsonObject config) {
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
    if (config.containsKey("jwt.alg")) {
      this.jwtConfig.put("algorithm", config.getString("jwt.alg"));
    }
    if (config.containsKey("jwt.audience")) {
      this.jwtConfig.put("audience", config.getString("jwt.audience"));
    }
    if (config.containsKey("jwt.issuer")) {
      this.jwtConfig.put("issuer", config.getString("jwt.issuer"));
    }
    if (config.containsKey("jwt.subject")) {
      this.jwtConfig.put("subject", config.getString("jwt.subject"));
    }
    if (config.containsKey("token.expires")) {
      this.jwtConfig.put("expiresInSeconds", config.getInteger("token.expires"));
    }
    this.expires = config.getInteger("token.expires", 1800);
    this.userKey = config.getString("jwt.userClaimKey", "userId");
    this.namespace = config.getString("namespace", "");
    String address = RedisProvider.class.getName();
    if (!Strings.isNullOrEmpty(namespace)) {
      address = namespace + "." + address;
    }
    this.permissionsKey = config.getString("jwt.permissionKey", "permissions");
    this.redisProvider = ProxyHelper.createProxy(RedisProvider.class, vertx, address);
  }

  @Override
  public String type() {
    return POST;
  }

  @Override
  public int order() {
    return 10000;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    if (apiContext.apiDefinition() == null) {
      return false;
    }
    return apiContext.apiDefinition()
                   .plugin(JwtBuildPlugin.class.getSimpleName()) != null;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    JsonObject jwtConfig = new JsonObject().put("keyStore", this.jwtConfig);
    JWTAuth provider = JWTAuth.create(vertx, jwtConfig);
    Result result = apiContext.result();
    if (!result.isArray()
        && result.statusCode() < 400
        && result.responseObject().containsKey(userKey)) {
      JsonObject body = result.responseObject();
      String jti = UUID.randomUUID().toString();
      String userId = body.getValue(userKey).toString();
      JsonObject claims = new JsonObject()
              .put("jti", jti)
              .put(userKey, userId);
      String userCacheKey = namespace + ":user:" + userId;
      JsonObject user = body.copy().put("jti", jti);
      String permissions = user.getString(permissionsKey, "all");
      user.put("permissions", permissions);
      redisProvider.setex(userCacheKey, user, expires, ar -> {
        if (ar.succeeded()) {
          try {
            String token = provider.generateToken(claims, new JWTOptions(this.jwtConfig));
            body.put("token", token);
            apiContext.setResult(Result.createJsonObject(result.statusCode(), body,
                                                         result.header()));
            LOGGER.info("---| [{}] [OK] [{}] [{}]",
                        apiContext.id(),
                        this.getClass().getSimpleName(),
                        "save token:" + userCacheKey);
            completeFuture.complete(apiContext);
          } catch (Exception e) {
            Log.create(LOGGER)
                    .setTraceId(apiContext.id())
                    .setEvent("token.generate.failed")
                    .setThrowable(e)
                    .error();
            completeFuture.fail(e);
          }
        } else {
          Log.create(LOGGER)
                  .setTraceId(apiContext.id())
                  .setEvent("token.generate.failed")
                  .setThrowable(ar.cause())
                  .error();
          completeFuture.fail(ar.cause());
        }
      });
    } else {
      completeFuture.complete(apiContext);
    }
  }

}
