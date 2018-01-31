package com.github.edgar615.direvolves.plugin.authentication;

import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.direwolves.core.dispatch.Result;
import com.github.edgar615.direwolves.core.utils.Log;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.auth.jwt.JWTOptions;

import java.util.ArrayList;
import java.util.List;
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
 * "jwt.builder": {
 * "expiresInSeconds" : 3600,//TOKEN过期秒数，可选 ，生成exp
 * "algorithm": "HS512", //算法，默认HS256
 *  "audience" : ["test"], 接收该JWT的一方，可选
 * "subject": "app", 该JWT所面向的用户，可选
 * "issuer" : "edgar615", 该JWT的签发者，可选
 * "noTimestamp" : false, 是否生成iat 默认true，不生成
 * "header" : {}, 额外的头信息，可选
 * "emptyingField" : true, //生成TOKEN时清除其他属性 默认false
 * "claimKey": [] //生成token时把除userId外的哪些属性存入claims
 * }
 * </pre>
 * keyStore配置
 * <pre>
 * "keyStore" : {
 * "path": "keystore.jceks", 证书路径
 * "type": "jceks", 证书类型
 * "password": "secret" 证书密码
 * }
 * </pre>
 * 该filter的order=10000
 * Created by edgar on 16-11-26.
 */
public class JwtBuildFilter implements Filter {

  private final Vertx vertx;

  private final String userKey = "userId";

  private final JWTAuthOptions jwtAuthOptions;

  private final JWTOptions jwtOptions;

  private final List<String> claimKey = new ArrayList<>();

  private boolean emptyingField = false;

  /**
   * @param vertx  Vertx
   * @param config 配置
   */
  JwtBuildFilter(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    if (config.getValue("jwt.builder") instanceof JsonObject) {
      JsonObject jwtBuilderConfig = config.getJsonObject("jwt.builder");
      this.jwtOptions = new JWTOptions(jwtBuilderConfig);
      if (jwtBuilderConfig.getValue("claimKey") instanceof JsonArray) {
        jwtBuilderConfig.getJsonArray("claimKey").forEach(item -> {
          if (item instanceof String) { claimKey.add((String) item); }
        });
      }
      if (jwtBuilderConfig.getValue("emptyingField") instanceof Boolean) {
        emptyingField = jwtBuilderConfig.getBoolean("emptyingField");
      }
    } else {
      this.jwtOptions = new JWTOptions();
    }
    //jwt
    this.jwtAuthOptions = new JWTAuthOptions();
    if (config.getValue("keyStore") instanceof JsonObject) {
      this.jwtAuthOptions
              .setKeyStore(new KeyStoreOptions(config.getJsonObject("keyStore")));
    } else {
      KeyStoreOptions keyStoreOptions = new KeyStoreOptions()
              .setPath("keystore.jceks")
              .setType("jceks")
              .setPassword("INIHPMOZPO");
      this.jwtAuthOptions.setKeyStore(keyStoreOptions);
    }
  }

  @Override
  public String type() {
    return POST;
  }

  @Override
  public int order() {
    return 20000;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    if (apiContext.apiDefinition() == null) {
      return false;
    }
    JwtBuildPlugin plugin = (JwtBuildPlugin) apiContext.apiDefinition()
            .plugin(JwtBuildPlugin.class.getSimpleName());
    if (plugin == null) {
      return false;
    }
    Result result = apiContext.result();
    if (result == null) {
      return false;
    }
    return !result.isArray()
           && result.statusCode() < 400
           && result.responseObject().containsKey(userKey);
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    JWTAuth provider = JWTAuth.create(vertx, jwtAuthOptions);
    Result result = apiContext.result();
    JsonObject body = result.responseObject();
    String jti = UUID.randomUUID().toString();
    Object userId = body.getValue(userKey);
    if (userId == null) {
      Log.create(Filter.LOGGER)
              .setEvent("jwt.build.ignored")
              .setMessage("Miss userId")
              .info();
      completeFuture.complete(apiContext);
      return;
    }
    JsonObject claims = new JsonObject()
            .put("jti", jti)
            .put(userKey, userId);
    claimKey.forEach(k -> {
      if (body.getValue(k) != null) {
        claims.put(k, body.getValue(k));
      }
    });
    String token = provider.generateToken(claims, jwtOptions);
    if (emptyingField) {
      body.clear().put("token", token);
    } else {
      body.put("token", token);
    }
    //保存JTI，后面使用
    apiContext.addVariable("jti", jti);
    apiContext.setResult(Result.createJsonObject(result.statusCode(), body,
                                                 result.headers()));
    completeFuture.complete(apiContext);
  }

}
