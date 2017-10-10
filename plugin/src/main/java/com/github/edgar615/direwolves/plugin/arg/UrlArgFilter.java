package com.github.edgar615.direwolves.plugin.arg;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.util.validation.Rule;
import com.github.edgar615.util.validation.Validations;
import io.vertx.core.Future;

import java.util.HashMap;
import java.util.Map;

/**
 * 请求参数校验的filter.
 * 如果未通过校验，直接抛出ValidationException异常
 * <p>
 * 该filter的order=100
 * Created by edgar on 16-10-28.
 */
public class UrlArgFilter implements Filter {
  UrlArgFilter() {
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
    return apiContext.apiDefinition().plugin(UrlArgPlugin.class.getSimpleName()) != null;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    UrlArgPlugin plugin =
            (UrlArgPlugin) apiContext.apiDefinition().plugin(UrlArgPlugin.class.getSimpleName());
    ApiContext newContext = checkDefaultValue(apiContext, plugin);

    final Multimap<String, Rule> rules = ArrayListMultimap.create();
    plugin.parameters().forEach(p -> rules.putAll(p.name(), p.rules()));
    Validations.validate(newContext.params(), rules);
    completeFuture.complete(newContext);
  }

  public ApiContext checkDefaultValue(ApiContext apiContext, UrlArgPlugin plugin) {
    Map<String, String>

            defaultMap = allocateDefaultValue(apiContext, plugin);
    Multimap<String, String> params = ArrayListMultimap.create(apiContext.params());
    defaultMap.forEach((k, v) -> {
      params.put(k, v);
    });
    ApiContext context =
            ApiContext.create(apiContext.id(), apiContext.method(), apiContext.path(), apiContext
                    .headers(), params, apiContext.body());
    ApiContext.copyProperites(apiContext, context);
    return context;
  }

  public Map<String, String> allocateDefaultValue(ApiContext apiContext, UrlArgPlugin plugin) {
    Map<String, String> defaultMap = new HashMap<>();
    plugin.parameters().stream()
            .filter(p -> p.defaultValue() != null)
            .forEach(p -> {
              if (!apiContext.params().containsKey(p.name())) {
                defaultMap.put(p.name(), p.defaultValue().toString());
              }
            });
    return defaultMap;
  }

}
