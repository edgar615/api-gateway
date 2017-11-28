package com.github.edgar615.direvolves.plugin.authentication;

import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.direwolves.core.dispatch.Result;
import com.github.edgar615.direwolves.core.utils.CacheUtils;
import com.github.edgar615.util.log.Log;
import com.github.edgar615.util.vertx.cache.Cache;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
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
 * </pre>
 * jwt配置
 * <pre>
 *     "jwt" : {
 * "ignoreExpiration" : false, 是否忽略exp，默认false，
 * "audiences" : [],JSON数组，接收方,默认null
 * "issuer": "", 签发方 默认null
 * "subject": "", 签发对象 默认null
 * "leeway": 0  允许的时间差 默认0
 * }
 * </pre>
 * <pre>
 *   "audiences" : []，
 *      "issuer": "",
 * "subject": "",
 * "leeway":
 *   "ignoreExpiration" :
 *   "permissionsClaimKey":用户权限字段 默认值permissions
 *   keystore.path 证书的路径，默认值keystore.jceks
 *   keystore.type 证书的类型，默认值jceks，可选值：JKS, JCEKS, PKCS12, BKS，UBER
 *   keystore.password 证书的密码，默认值secret
 *   jwt.alg 证书的算法，默认值HS512
 *  token.expires int token的过期时间exp，单位秒，默认值1800
 *
 *  jwt.userClaimKey token中的用户主键，默认值userId
 *   jwt.permissionKey 用户权限字段 默认值permissions
 * </pre>
 * keyStore配置
 * <pre>
 *     "keyStore" : {
 * "path": "keystore.jceks", 证书路径
 * "type": "jceks", 证书类型
 * "password": "secret" 证书密码
 * }
 * </pre>
 * 该filter的order=10000
 * Created by edgar on 16-11-26.
 */
public class JtiStoreFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(JtiStoreFilter.class);

  private final Vertx vertx;

  private final String userKey = "userId";

  private final Cache<String, JsonObject> userCache;

  private final String namespace;

  /**
   * @param vertx  Vertx
   * @param config 配置
   */
  JtiStoreFilter(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    this.namespace = config.getString("namespace", "api-gateway");

    //user
    JsonObject userConfig = config.getJsonObject("user", new JsonObject());

    if (userConfig.getValue("cache") instanceof JsonObject) {
      this.userCache = CacheUtils.createCache(vertx, "userCache",
                                              userConfig.getJsonObject("cache"));
    } else {
      this.userCache = CacheUtils.createCache(vertx, "userCache", new JsonObject());
    }
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
//    userCache.put(userCacheKey, user, ar -> {
//      if (ar.succeeded()) {
//        try {
//          String token = provider.generateToken(claims, jwtOptions);
//          body.put("token", token);
//          apiContext.setResult(Result.createJsonObject(result.statusCode(), body,
//                  result.header()));
//          LOGGER.info("---| [{}] [OK] [{}] [{}]",
//                  apiContext.id(),
//                  this.getClass().getSimpleName(),
//                  "save token:" + userCacheKey);
//          completeFuture.complete(apiContext);
//        } catch (Exception e) {
//          Log.create(LOGGER)
//                  .setTraceId(apiContext.id())
//                  .setEvent("token.generate.failed")
//                  .setThrowable(e)
//                  .error();
//          completeFuture.fail(e);
//        }
//      } else {
//        Log.create(LOGGER)
//                .setTraceId(apiContext.id())
//                .setEvent("token.generate.failed")
//                .setThrowable(ar.cause())
//                .error();
//        completeFuture.fail(ar.cause());
//      }
//    });
  }

}
