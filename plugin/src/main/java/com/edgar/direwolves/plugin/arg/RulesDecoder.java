package com.edgar.direwolves.plugin.arg;

import com.edgar.util.validation.Rule;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * 从JsonObject转换为Parameter对象
 *
 * @author Edgar  Date 2016/9/30
 */
class RulesDecoder implements Function<JsonObject, List<Rule>> {

  private static final RulesDecoder INSTANCE = new RulesDecoder();

  private RulesDecoder() {
  }

  static Function<JsonObject, List<Rule>> instance() {
    return INSTANCE;
  }

  @Override
  public List<Rule> apply(JsonObject jsonObject) {
    return rules(jsonObject);
  }

  private List<Rule> rules(JsonObject jsonObject) {
    List<Rule> rules = new ArrayList<>();
    jsonObject.getMap().forEach((key, value) -> {
      if ("required".equals(key) &&
          "true".equals(value.toString())) {
        rules.add(Rule.required());
      }
      if ("max_length".equals(key)) {
        rules.add(Rule.maxLength((Integer) value));
      }
      if ("min_length".equals(key)) {
        rules.add(Rule.minLength((Integer) value));
      }
      if ("max".equals(key)) {
        rules.add(Rule.max((Integer) value));
      }
      if ("min".equals(key)) {
        rules.add(Rule.min((Integer) value));
      }
      if ("regex".equals(key)) {
        rules.add(Rule.regex((String) value));
      }
      if ("prohibited".equals(key) &&
          "true".equals(value.toString())) {
        rules.add(Rule.prohibited());
      }
      if ("email".equals(key) &&
          "true".equals(value.toString())) {
        rules.add(Rule.email());
      }
      if ("integer".equals(key) &&
          "true".equals(value.toString())) {
        rules.add(Rule.integer());
      }
      if ("bool".equals(key) &&
          "true".equals(value.toString())) {
        rules.add(Rule.bool());
      }
      if ("list".equals(key) &&
          "true".equals(value.toString())) {
        rules.add(Rule.list());
      }
      if ("map".equals(key) &&
          "true".equals(value.toString())) {
        rules.add(Rule.map());
      }
      if ("equals".equals(key)) {
        rules.add(Rule.equals(value.toString()));
      }
      if ("optional".equals(key)) {
        if (value instanceof Collection) {
          rules.add(Rule.optional(ImmutableList.copyOf((Collection) value)));
        } else {
          Iterable<String> iterable =
                  Splitter.on(",").trimResults().omitEmptyStrings().split(value.toString());
          rules.add(Rule.optional(ImmutableList.copyOf(iterable)));
        }
      }
    });
    return rules;
  }
}