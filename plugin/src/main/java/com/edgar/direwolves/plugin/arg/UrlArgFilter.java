package com.edgar.direwolves.plugin.arg;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.util.validation.Rule;
import com.edgar.util.validation.Validations;
import io.vertx.core.Future;

import java.util.HashMap;
import java.util.Map;

/**
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
    ApiContext context = ApiContext.create(apiContext.method(), apiContext.path(), apiContext
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
