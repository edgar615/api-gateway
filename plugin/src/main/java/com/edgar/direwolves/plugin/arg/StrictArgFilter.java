package com.edgar.direwolves.plugin.arg;

import com.google.common.collect.ArrayListMultimap;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.util.validation.ValidationException;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by edgar on 16-10-28.
 */
public class StrictArgFilter implements Filter {

  private final Set<String> excludeQuery = new HashSet<>();

  private final Set<String> excludeBody = new HashSet<>();

  private final boolean enabled;

  StrictArgFilter(JsonObject config) {
    JsonArray queryArray = config.getJsonArray("strict_arg.query.excludes", new JsonArray());
    for (int i = 0; i < queryArray.size(); i++) {
      excludeQuery.add(queryArray.getString(i));
    }
    JsonArray bodyArray = config.getJsonArray("strict_arg.body.excludes", new JsonArray());
    for (int i = 0; i < bodyArray.size(); i++) {
      excludeBody.add(bodyArray.getString(i));
    }
    this.enabled = config.getBoolean("strict_arg", false);
  }

  @Override
  public String type() {
    return PRE;
  }

  @Override
  public int order() {
    return 90;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    return enabled;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    UrlArgPlugin urlArgPlugin =
            (UrlArgPlugin) apiContext.apiDefinition().plugin(UrlArgPlugin.class.getSimpleName());

    ArrayListMultimap error = ArrayListMultimap.create();
    if (!apiContext.params().isEmpty()) {
      apiContext.params().keySet().stream()
              .filter(k -> testUrlArg(k, urlArgPlugin))
              .forEach(k -> error.put(k, "prohibited"));
    }
    if (!error.isEmpty()) {
      throw new ValidationException(error);
    }

    BodyArgPlugin bodyArgPlugin =
            (BodyArgPlugin) apiContext.apiDefinition().plugin(BodyArgPlugin.class.getSimpleName());
    ArrayListMultimap bodyError = ArrayListMultimap.create();
    if (apiContext.body() != null) {
      apiContext.body().fieldNames().stream()
              .filter(k -> testBodyArg(k, bodyArgPlugin))
              .forEach(k -> bodyError.put(k, "prohibited"));
    }
    if (!bodyError.isEmpty()) {
      throw new ValidationException(bodyError);
    }
    completeFuture.complete(apiContext);
  }

  private boolean testBodyArg(String argName, BodyArgPlugin plugin) {
    if (excludeBody.contains(argName)) {
      return false;
    }
    return plugin == null || plugin.parameter(argName) == null;
  }

  private boolean testUrlArg(String argName, UrlArgPlugin plugin) {
    if (excludeQuery.contains(argName)) {
      return false;
    }
    return plugin == null || plugin.parameter(argName) == null;
  }

}
