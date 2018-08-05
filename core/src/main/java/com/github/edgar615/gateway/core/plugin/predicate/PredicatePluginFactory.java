package com.github.edgar615.gateway.core.plugin.predicate;

import com.github.edgar615.gateway.core.definition.ApiPlugin;
import com.github.edgar615.gateway.core.definition.ApiPluginFactory;
import com.google.common.base.Preconditions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class PredicatePluginFactory implements ApiPluginFactory {
  @Override
  public String name() {
    return PredicatePlugin.class.getSimpleName();
  }

  @Override
  public ApiPlugin create() {
    return new PredicatePlugin();
  }

  @Override
  public ApiPlugin decode(JsonObject jsonObject) {
    if (jsonObject.getValue("predicate") instanceof JsonObject) {
      PredicatePlugin plugin = new PredicatePlugin();
      JsonObject predicate = jsonObject.getJsonObject("predicate");
      if (predicate.getValue("before") instanceof String) {
        plugin.add(new BeforePredicate(predicate.getString("before")));
      }
      if (predicate.getValue("after") instanceof String) {
        plugin.add(new BeforePredicate(predicate.getString("after")));
      }
      if (predicate.getValue("between") instanceof JsonArray) {
        JsonArray between = predicate.getJsonArray("between");
        Preconditions.checkArgument(between.size() == 2, "between must has 2 element");
        plugin.add(new BetweenPredicate(between.getString(0), between.getString(1)));
      }
      if (predicate.getValue("headerContains") instanceof JsonArray) {
        JsonArray headerContains = predicate.getJsonArray("headerContains");
        plugin.add(new HeaderContainsPredicate(headerContains.getList()));
      }
      if (predicate.getValue("headerEquals") instanceof JsonObject) {
        JsonObject headerEquals = predicate.getJsonObject("headerEquals");
        plugin.add(new HeaderEqualsPredicate(transform(headerEquals)));
      }
      if (predicate.getValue("headerRegex") instanceof JsonObject) {
        JsonObject headerRegex = predicate.getJsonObject("headerRegex");
        plugin.add(new HeaderRegexPredicate(transform(headerRegex)));
      }
      if (predicate.getValue("queryContains") instanceof JsonArray) {
        JsonArray queryContains = predicate.getJsonArray("queryContains");
        plugin.add(new QueryContainsPredicate(queryContains.getList()));
      }
      if (predicate.getValue("queryEquals") instanceof JsonObject) {
        JsonObject queryEquals = predicate.getJsonObject("queryEquals");
        plugin.add(new QueryEqualsPredicate(transform(queryEquals)));
      }
      if (predicate.getValue("queryRegex") instanceof JsonObject) {
        JsonObject queryRegex = predicate.getJsonObject("queryRegex");
        plugin.add(new QueryRegexPredicate(transform(queryRegex)));
      }
    }
    return null;
  }

  private Map<String, String> transform(JsonObject jsonObject) {
    Map<String, String> map = new HashMap<>();
    for (String key : jsonObject.fieldNames()) {
      map.put(key, jsonObject.getString(key));
    }
    return map;
  }

  @Override
  public JsonObject encode(ApiPlugin obj) {
    return null;
  }
}
