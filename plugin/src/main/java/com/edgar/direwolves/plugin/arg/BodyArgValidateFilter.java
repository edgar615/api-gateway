package com.edgar.direwolves.plugin.arg;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.validation.Rule;
import com.edgar.util.validation.Validations;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 16-10-28.
 */
public class BodyArgValidateFilter implements Filter {

  public static final String NAME = "BODY_ARG_VALIDATE";

  @Override
  public String type() {
    return PRE;
  }

  @Override
  public int order() {
    return 100;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    return apiContext.apiDefinition().plugin(BodyArgPlugin.class.getSimpleName()) != null;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    BodyArgPlugin plugin =
            (BodyArgPlugin) apiContext.apiDefinition().plugin(BodyArgPlugin.class.getSimpleName());
    if (apiContext.body() == null) {
      throw SystemException.create(DefaultErrorCode.INVALID_JSON);
    }
    //设置默认值
    plugin.parameters().stream()
            .filter(p -> p.defaultValue() != null)
            .forEach(p -> {
              if (!apiContext.body().containsKey(p.name())) {
                apiContext.body().put(p.name(), p.defaultValue().toString());
              }
            });
    //校验
    final Multimap<String, Rule> rules = ArrayListMultimap.create();
    plugin.parameters().forEach(p -> rules.putAll(p.name(), p.rules()));
    Validations.validate(apiContext.body().getMap(), rules);
    completeFuture.complete(apiContext);
  }

  @Override
  public void config(Vertx vertx, JsonObject config) {

  }
}
