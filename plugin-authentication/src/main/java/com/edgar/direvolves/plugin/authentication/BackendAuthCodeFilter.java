package com.edgar.direvolves.plugin.authentication;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.utils.Helper;
import com.edgar.util.base.EncryptUtils;
import com.edgar.util.base.Randoms;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.validation.Rule;
import com.edgar.util.validation.Validations;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * 隐藏的一个过滤器，主要用于超级管理员的访问授权.
 * 这个Filter需要body中有tel参数，会保存backend.code、backend.sign两个变量
 * 该filter可以接受下列的配置参数
 * <pre>
 *   backend.permitted JSON数组 允许的(用户名)
 * </pre>
 * 该filter的order=1000
 *
 * @author Edgar  Date 2017/3/10
 */
public class BackendAuthCodeFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(BackendAuthCodeFilter.class);

  private final Multimap<String, Rule> commonParamRule = ArrayListMultimap.create();

  private final Set<String> allowedPermitted = new ConcurrentSkipListSet<>();

  private final Vertx vertx;

  BackendAuthCodeFilter(Vertx vertx, JsonObject config) {
    commonParamRule.put("username", Rule.required());
    this.vertx = vertx;
    JsonArray permitted = config.getJsonArray("backend.permitted", new JsonArray());
    for (int i = 0; i < permitted.size(); i++) {
      allowedPermitted.add(permitted.getString(i));
    }
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
                   .plugin(BackendAuthCodePlugin.class.getSimpleName()) != null;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    if (apiContext.body() == null) {
      Validations.validate(new HashMap<>(), commonParamRule);
    } else {
      Validations.validate(apiContext.body().getMap(), commonParamRule);
    }
    String username = apiContext.body().getString("username");
    if (!allowedPermitted.contains(username)) {
      Helper.logFailed(LOGGER, apiContext.id(),
                       this.getClass().getSimpleName(),
                       username + " not allowed");
      throw SystemException.create(DefaultErrorCode.PERMISSION_DENIED)
              .set("details", username + " not allowed");
    }
    String code = Randoms.randomNumber(6);
    apiContext.addVariable("backend.code", code);

    long exp = Instant.now().getEpochSecond() + 60 * 5;
    String chaim = new JsonObject().put("exp", exp).encode();
    String chaimSeg = base64urlEncode(chaim);
    try {
      String sign = EncryptUtils.encryptHmacMd5(chaimSeg, code + exp);
      apiContext.addVariable("backend.sign", chaimSeg + "." + sign);
      completeFuture.complete(apiContext);
    } catch (IOException e) {
      Helper.logFailed(LOGGER, apiContext.id(),
                       this.getClass().getSimpleName(),
                       e.getMessage());
      throw SystemException.wrap(DefaultErrorCode.UNKOWN, e);
    }
  }

  private String base64urlEncode(String str) {
    return new String(Base64.getUrlEncoder().encode(str.getBytes(StandardCharsets.UTF_8)));
  }
}
