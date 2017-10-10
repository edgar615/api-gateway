package com.github.edgar615.direwolves.plugin.arg;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import com.github.edgar615.util.validation.Rule;
import com.github.edgar615.util.validation.Validations;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 请求体校验的filter.
 * 如果未通过校验，直接抛出ValidationException异常
 * <p>
 * 该filter的order=100
 * Created by edgar on 16-10-28.
 */
public class BodyArgFilter implements Filter {

  BodyArgFilter() {
  }

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
      throw SystemException.create(DefaultErrorCode.INVALID_JSON)
              .set("details", "Request body must be JSON Object");
    }
    ApiContext newContext = checkDefaultValue(apiContext, plugin);
    //校验
    final Multimap<String, Rule> rules = ArrayListMultimap.create();
    plugin.parameters().forEach(p -> rules.putAll(p.name(), p.rules()));
    Validations.validate(newContext.body().getMap(), rules);
    completeFuture.complete(newContext);
  }

  public ApiContext checkDefaultValue(ApiContext apiContext, BodyArgPlugin plugin) {
    Map<String, String> defaultMap = allocateDefaultValue(apiContext, plugin);
    JsonObject body = apiContext.body().copy();
    defaultMap.forEach((k, v) -> {
      body.put(k, v);
    });
    ApiContext context =
            ApiContext.create(apiContext.id(), apiContext.method(), apiContext.path(), apiContext
                    .headers(), apiContext.params(), body);
    ApiContext.copyProperites(apiContext, context);
    return context;
  }

  public Map<String, String> allocateDefaultValue(ApiContext apiContext, BodyArgPlugin plugin) {
    Map<String, String> defaultMap = new HashMap<>();
    plugin.parameters().stream()
            .filter(p -> p.defaultValue() != null)
            .forEach(p -> {
              if (!apiContext.body().containsKey(p.name())) {
                defaultMap.put(p.name(), p.defaultValue().toString());
              }
            });
    return defaultMap;
  }

}
