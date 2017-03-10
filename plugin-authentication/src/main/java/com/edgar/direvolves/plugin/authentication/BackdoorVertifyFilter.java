package com.edgar.direvolves.plugin.authentication;

import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.util.base.EncryptUtils;
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
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * 隐藏的一个过滤器，主要用于超级管理员的访问授权.
 * 校验用户的验证码和时间是否正确.正确返回一个TOKEN
 *
 * @author Edgar  Date 2017/3/10
 */
public class BackdoorVertifyFilter implements Filter {
  private final Multimap<String, Rule> commonParamRule = ArrayListMultimap.create();

  private final Set<String> allowedPermitted = new ConcurrentSkipListSet<>();

  private final Vertx vertx;

  BackdoorVertifyFilter(Vertx vertx, JsonObject config) {
    commonParamRule.put("tel", Rule.required());
    commonParamRule.put("code", Rule.required());
    commonParamRule.put("sign", Rule.required());
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
                   .plugin(BackdoorVertifyPlugin.class.getSimpleName()) != null;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    Validations.validate(apiContext.params(), commonParamRule);
    String tel = apiContext.params().get("tel").iterator().next();
    if (!allowedPermitted.contains(tel)) {
      throw SystemException.create(DefaultErrorCode.NO_AUTHORITY)
              .set("details", "the tel not allowed:" + tel);
    }
    String code = apiContext.params().get("code").iterator().next();
    String clientSign = apiContext.params().get("sign").iterator().next();
    List<String> splits = Splitter.on(".").splitToList(clientSign);

    JsonObject chaim = new JsonObject(splits.get(0));
    long exp = chaim.getLong("exp", Instant.now().getEpochSecond() - 10 * 60);
    if (exp < (Instant.now().getEpochSecond() - 5 * 60)) {
      throw SystemException.create(DefaultErrorCode.EXPIRE);
    }

    try {
      String serverSign = EncryptUtils.encryptHmacMd5(splits.get(0), code + exp);
      if (serverSign.equalsIgnoreCase(clientSign)) {
        completeFuture.complete(apiContext);
      } else {
        throw SystemException.create(DefaultErrorCode.NAME_PWD_INCORRECT);
      }
    } catch (IOException e) {
      throw SystemException.wrap(DefaultErrorCode.UNKOWN, e);
    }
  }
}
