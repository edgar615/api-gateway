package com.github.edgar615.direvolves.plugin.authentication;

import com.github.edgar615.direwolves.core.cache.Cache;
import com.github.edgar615.direwolves.core.cache.CacheFactory;
import com.github.edgar615.direwolves.core.cache.CacheManager;
import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.direwolves.core.dispatch.Result;
import com.github.edgar615.util.log.Log;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTOptions;
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

  private final String userKey;

  private final Cache userCache;

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
    this.namespace = config.getString("namespace", "");
    //jwt
    this.jwtConfig.mergeIn(config.getJsonObject("jwt", new JsonObject()));

    //user
    JsonObject userConfig = config.getJsonObject("user", new JsonObject());
    this.userKey = userConfig.getString("userClaimKey", "userId");
    this.permissionsKey = userConfig.getString("permissionKey", "permissions");

    //cache
    String cacheType = config.getString("cache", "local");
    CacheFactory factory = CacheFactory.get(cacheType);

    JsonObject cacheConfig = config.copy();
    if (jwtConfig.getValue("expiresInSeconds") instanceof Number) {
      cacheConfig.put("expireAfterWrite",
                      ((Number) jwtConfig.getValue("expiresInSeconds")).longValue());
    }
    //由于redis的缓存需要redis的属性，所有不能单纯的用cacheConfig来创建cache
    this.userCache = factory.create(vertx, "userCache", cacheConfig);
    CacheManager.instance().addCache(userCache);
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
      userCache.put(userCacheKey, user, ar -> {
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
