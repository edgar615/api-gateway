//package com.edgar.direwolves.definition;
//
//import com.google.common.base.Preconditions;
//
//import com.edgar.util.validation.Rule;
//import io.vertx.core.json.JsonObject;
//
//import java.util.List;
//import java.util.function.Function;
//
///**
// * 从JsonObject转换为Parameter对象
// *
// * @author Edgar  Date 2016/9/30
// */
//class ParameterDecoder implements Function<JsonObject, Parameter> {
//
//  private static final ParameterDecoder INSTANCE = new ParameterDecoder();
//
//  private ParameterDecoder() {
//  }
//
//  static Function<JsonObject, Parameter> instance() {
//    return INSTANCE;
//  }
//
//  @Override
//  public Parameter apply(JsonObject jsonObject) {
//    String name = jsonObject.getString("name");
//    Preconditions.checkNotNull(name, "arg name cannot be null");
//    Object defaultValue = jsonObject.getValue("default_value");
//    Parameter parameter = Parameter.create(name, defaultValue);
//    List<Rule> rules = RulesDecoder.instance().apply(jsonObject.getJsonObject("rules", new
//            JsonObject()));
//    rules.forEach(rule -> parameter.addRule(rule));
//    return parameter;
//  }
//
//}
