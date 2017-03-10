package com.edgar.direvolves.plugin.authentication;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
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

import java.io.IOException;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * 隐藏的一个过滤器，主要用于超级管理员的访问授权.
 * 请求登录密码，密码是一个随机的6位数字，会通过短信发送到手机上，有五分钟的失效性
 *
 * @author Edgar  Date 2017/3/10
 */
public class BackdoorAuthFilter implements Filter {
  private final Multimap<String, Rule> commonParamRule = ArrayListMultimap.create();

  private final Set<String> allowedPermitted = new ConcurrentSkipListSet<>();

  private final Vertx vertx;

  BackdoorAuthFilter(Vertx vertx, JsonObject config) {
    commonParamRule.put("tel", Rule.required());
    this.vertx = vertx;
    JsonArray permitted = config.getJsonArray("backdoor.permitted", new JsonArray());
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
                   .plugin(BackdoorAuthPlugin.class.getSimpleName()) != null;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    Validations.validate(apiContext.params(), commonParamRule);
    String tel = apiContext.params().get("tel").iterator().next();
    if (!allowedPermitted.contains(tel)) {
      throw SystemException.create(DefaultErrorCode.NO_AUTHORITY)
              .set("details", "the tel not allowed:" + tel);
    }
    String code = Randoms.randomNumber(6);
    apiContext.addVariable("backdoor.code", code);

    long exp = Instant.now().getEpochSecond() + 60 * 5;
    String chaim = new JsonObject().put("exp", exp).encode();
    try {
      String sign = EncryptUtils.encryptHmacMd5(chaim, code + exp);
      apiContext.addVariable("backdoor.sign", chaim + "." + sign);
      completeFuture.complete(apiContext);
    } catch (IOException e) {
      throw SystemException.wrap(DefaultErrorCode.UNKOWN, e);
    }
  }
}
