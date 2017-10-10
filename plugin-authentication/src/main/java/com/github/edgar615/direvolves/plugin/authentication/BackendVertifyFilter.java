package com.github.edgar615.direvolves.plugin.authentication;

import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.util.log.Log;
import com.github.edgar615.util.base.EncryptUtils;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import com.github.edgar615.util.validation.Rule;
import com.github.edgar615.util.validation.Validations;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * 隐藏的一个过滤器，主要用于超级管理员的访问授权.
 * 校验用户的验证码和时间是否正确.正确返回一个TOKEN
 *
 * @author Edgar  Date 2017/3/10
 */
public class BackendVertifyFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(BackendVertifyFilter.class);

  private final Multimap<String, Rule> commonParamRule = ArrayListMultimap.create();

  private final Set<String> allowedPermitted = new ConcurrentSkipListSet<>();

  private final Vertx vertx;

  BackendVertifyFilter(Vertx vertx, JsonObject config) {
    commonParamRule.put("username", Rule.required());
    commonParamRule.put("code", Rule.required());
    commonParamRule.put("sign", Rule.required());
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
                   .plugin(BackendVertifyPlugin.class.getSimpleName()) != null;
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
      Log.create(LOGGER)
              .setTraceId(apiContext.id())
              .setEvent("backend.authentication.failed")
              .setMessage("{} not allowed")
              .addArg(username)
              .error();
      throw SystemException.create(DefaultErrorCode.PERMISSION_DENIED)
              .set("details", username + " not allowed");
    }
    try {
      String code = apiContext.body().getString("code");
      String clientSign = apiContext.body().getString("sign");
      List<String> splits = Splitter.on(".").splitToList(clientSign);

      JsonObject chaim = new JsonObject(base64urlDecode(splits.get(0)));
      long exp = chaim.getLong("exp", Instant.now().getEpochSecond() - 10 * 60);
      if (exp < (Instant.now().getEpochSecond() - 5 * 60)) {
        throw SystemException.create(DefaultErrorCode.EXPIRE);
      }
      String serverSign = EncryptUtils.encryptHmacMd5(splits.get(0), code + exp);
      if (serverSign.equalsIgnoreCase(splits.get(1))) {
        completeFuture.complete(apiContext);
      } else {
        throw SystemException.create(DefaultErrorCode.UNKOWN_ACCOUNT);
      }
    } catch (SystemException e) {
      Log.create(LOGGER)
              .setTraceId(apiContext.id())
              .setEvent("backend.authentication.failed")
              .setThrowable(e)
              .error();
      throw e;
    } catch (Exception e) {
      Log.create(LOGGER)
              .setTraceId(apiContext.id())
              .setEvent("backend.authentication.failed")
              .setThrowable(e)
              .error();
      throw SystemException.wrap(DefaultErrorCode.UNKOWN_ACCOUNT, e);
    }
  }

  private String base64urlDecode(String str) {
    return new String(Base64.getUrlDecoder().decode(str.getBytes(StandardCharsets.UTF_8)));
  }
}
