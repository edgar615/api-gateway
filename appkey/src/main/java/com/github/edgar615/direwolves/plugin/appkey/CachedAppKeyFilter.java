package com.github.edgar615.direwolves.plugin.appkey;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.direwolves.core.utils.CacheUtils;
import com.github.edgar615.direwolves.core.utils.Consts;
import com.github.edgar615.direwolves.core.utils.MultimapUtils;
import com.github.edgar615.util.base.EncryptUtils;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import com.github.edgar615.util.validation.Rule;
import com.github.edgar615.util.validation.Validations;
import com.github.edgar615.util.vertx.cache.Cache;
import com.github.edgar615.util.vertx.cache.CacheLoader;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


/**
 * AppKey的信息保存在缓存中，没有缓存就从下哟服务获取.
 * 但是这样会让网关承担了过多的职责，因此不再支持这种做法，在这里仅仅是做一个备份而已
 */
@Deprecated
public class CachedAppKeyFilter implements Filter {

  private final Multimap<String, Rule> commonParamRule = ArrayListMultimap.create();

  private final String namespace;

  private final Vertx vertx;

  private final Cache<String, JsonObject> cache;

  private final CacheLoader<String, JsonObject> appKeyLoader;

  private final String NOT_EXISTS_KEY = UUID.randomUUID().toString();

  CachedAppKeyFilter(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    commonParamRule.put("appKey", Rule.required());
    commonParamRule.put("nonce", Rule.required());
    commonParamRule.put("signMethod", Rule.required());
    List<Object> optionalRule = new ArrayList<>();
    optionalRule.add("HMACSHA256");
    optionalRule.add("HMACSHA512");
    optionalRule.add("HMACMD5");
    optionalRule.add("MD5");
    commonParamRule.put("signMethod", Rule.optional(optionalRule));
    commonParamRule.put("sign", Rule.required());
    this.namespace = config.getString("namespace", Consts.DEFAULT_NAMESPACE);
    JsonObject appKeyConfig = config.getJsonObject("appkey", new JsonObject());
    if (appKeyConfig.getValue("cache") instanceof JsonObject) {
      this.cache = CacheUtils.createCache(vertx, "appKeyCache",
                                          appKeyConfig.getJsonObject("cache"));
    } else {
      this.cache = CacheUtils.createCache(vertx, "appKeyCache", new JsonObject());
    }

    appKeyConfig.put("notExistsKey", NOT_EXISTS_KEY);
    appKeyConfig.put("port", config.getInteger("port", Consts.DEFAULT_PORT));
    appKeyLoader = new AppKeyLoader(vertx, namespace + ":ak:", appKeyConfig);
  }

  @Override
  public String type() {
    return PRE;
  }

  @Override
  public int order() {
    return 8000;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    return apiContext.apiDefinition().plugin(AppKeyPlugin.class.getSimpleName()) != null;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    //校验参数
    Validations.validate(apiContext.params(), commonParamRule);
    Multimap<String, String> params = ArrayListMultimap.create(apiContext.params());
    String clientSignValue = MultimapUtils.getFirst(params, "sign").toString();
    String signMethod = MultimapUtils.getFirst(params, "signMethod").toString();
    String appKey = MultimapUtils.getFirst(params, "appKey").toString();
    params.removeAll("sign");
    if (apiContext.body() != null) {
      params.removeAll("body");
      params.put("body", apiContext.body().encode());
    }
    cache.get(wrapKey(appKey), appKeyLoader, ar -> {
      if (ar.failed() || ar.result().containsKey(NOT_EXISTS_KEY)) {
        SystemException e = SystemException.create(DefaultErrorCode.INVALID_REQ)
                .set("details", "Undefined AppKey:" + appKey);
        failed(completeFuture, apiContext.id(), "appKey.tripped", e);
        return;
      }
      JsonObject jsonObject = ar.result();
      checkSign(apiContext, completeFuture, params, clientSignValue, signMethod, jsonObject);
    });

  }

  private void checkSign(ApiContext apiContext, Future<ApiContext> completeFuture,
                         Multimap<String, String> params, String clientSignValue, String signMethod,
                         JsonObject app) {
    String secret = app.getString("appSecret", "UNKOWNSECRET");
    String serverSignValue = signTopRequest(params, secret, signMethod);
    if (!clientSignValue.equalsIgnoreCase(serverSignValue)) {
      SystemException e = SystemException.create(DefaultErrorCode.INVALID_REQ)
              .set("details", "Incorrect sign");
      failed(completeFuture, apiContext.id(), "appKey.tripped", e);
    } else {
      apiContext.addVariable("client.appKey", app.getString("appKey", "anonymous"));
      if (app.containsKey("appId")) {
        apiContext.addVariable("client.appId", app.getValue("appId"));
      }
      if (app.containsKey("permissions")) {
        apiContext.addVariable("client.permissions", app.getValue("permissions"));
      }
      completeFuture.complete(apiContext);
    }
  }

  private String wrapKey(String appkey) {
    return namespace + ":ak:" + appkey;
  }

  private String signTopRequest(Multimap<String, String> params, String secret, String signMethod) {
    String queryString = baseString(params);

    String sign = null;
    try {
      if (EncryptUtils.HMACMD5.equalsIgnoreCase(signMethod)) {
        sign = EncryptUtils.encryptHmacMd5(queryString, secret);
      } else if (EncryptUtils.HMACSHA256.equalsIgnoreCase(signMethod)) {
        sign = EncryptUtils.encryptHmacSha256(queryString, secret);
      } else if (EncryptUtils.HMACSHA512.equalsIgnoreCase(signMethod)) {
        sign = EncryptUtils.encryptHmacSha512(queryString, secret);
      } else if (EncryptUtils.MD5.equalsIgnoreCase(signMethod)) {
        sign = EncryptUtils.encryptMD5(secret + queryString + secret);
      }
    } catch (IOException e) {

    }
    return sign;
  }

  private String baseString(Multimap<String, String> params) {// 第一步：检查参数是否已经排序
    String[] keys = params.keySet().toArray(new String[0]);
    Arrays.sort(keys);

    // 第二步：把所有参数名和参数值串在一起
    List<String> query = new ArrayList<>(params.size());
    for (String key : keys) {
      if (!key.startsWith("$param")) {
        String value = MultimapUtils.getFirst(params, key);
        if (!Strings.isNullOrEmpty(value)) {
          query.add(key + "=" + value);
        }
      }
    }
    return Joiner.on("&").join(query);
  }
}