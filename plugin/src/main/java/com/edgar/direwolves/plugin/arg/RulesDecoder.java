package com.edgar.direwolves.plugin.arg;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

import com.edgar.util.validation.Rule;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * 从JsonObject转换为Parameter对象.
 * <p>
 * <pre>
 *   required : true 必填项
 *   maxLength : 整数 最大长度
 *   minLength : 整数 最小长度
 *   fixLength : 整数 固定长度
 *   max : 整数 最大值
 *   min : 整数 最小值
 *    regex : 正则表达式 正则校验
 *    prohibited : true 非法参数
 *    email : true 邮箱
 *    integer : true 整数
 *    bool : true bool值
 *    list : true 数组
 *    map : true map
 *    optional : 数组 可选的值
 *    equals : 字符串 必须相等
 * </pre>
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
      if ("maxLength".equals(key)) {
        rules.add(Rule.maxLength((Integer) value));
      }
      if ("minLength".equals(key)) {
        rules.add(Rule.minLength((Integer) value));
      }
      if ("fixLength".equals(key)) {
        rules.add(Rule.fixLength((Integer) value));
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
        } else if (value instanceof JsonArray) {
          JsonArray array = (JsonArray) value;
          rules.add(Rule.optional(ImmutableList.copyOf(array.getList())));
        }else {
          Iterable<String> iterable =
                  Splitter.on(",").trimResults().omitEmptyStrings().split(value.toString());
          rules.add(Rule.optional(ImmutableList.copyOf(iterable)));
        }
      }
    });
    return rules;
  }
}
