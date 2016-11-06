package com.edgar.direwolves.plugin.arg;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.util.validation.Rule;
import com.edgar.util.validation.Validations;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 16-10-28.
 */
public class UrlArgValidateFilter implements Filter {

  public static final String NAME = "URL_ARG_VALIDATE";

  @Override
  public String name() {
    return NAME;
  }

  @Override
  public String type() {
    return PRE;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    return apiContext.apiDefinition().plugin(UrlArgPlugin.NAME) != null;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    UrlArgPlugin plugin = (UrlArgPlugin) apiContext.apiDefinition().plugin(UrlArgPlugin.NAME);
    //设置默认值
    plugin.parameters().stream()
        .filter(p -> p.defaultValue() != null)
        .forEach(p -> {
          if (!apiContext.params().containsKey(p.name())) {
            apiContext.params().put(p.name(), p.defaultValue().toString());
          }
        });
    //校验
    final Multimap<String, Rule> rules = ArrayListMultimap.create();
    plugin.parameters().forEach(p -> rules.putAll(p.name(), p.rules()));
    Validations.validate(apiContext.params(), rules);
    completeFuture.complete(apiContext);
  }

  @Override
  public void config(Vertx vertx, JsonObject config) {

  }
}
