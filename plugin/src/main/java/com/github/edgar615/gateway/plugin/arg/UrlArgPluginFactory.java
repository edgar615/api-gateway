package com.github.edgar615.gateway.plugin.arg;

import com.google.common.base.Preconditions;

import com.github.edgar615.gateway.core.definition.ApiPlugin;
import com.github.edgar615.gateway.core.definition.ApiPluginFactory;
import com.github.edgar615.util.validation.Rule;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * URL参数控制的工厂类.
 * json配置
 * <pre>
 *   "url.arg" : [
 * {
 * "name" : "limit",
 * "default_value" : 10,
 * "rules" : {
 * "integer":true,
 * "max":100,
 * "min":1
 * }
 * },
 * {
 * "name" : "start",
 * "default_value" : 0,
 * "rules" : {
 * "integer":true
 * }
 * }
 * ]
 * </pre>
 * Created by edgar on 16-10-22.
 */
public class UrlArgPluginFactory implements ApiPluginFactory {

  @Override
  public String name() {
    return UrlArgPlugin.class.getSimpleName();
  }

  @Override
  public ApiPlugin create() {
    return new UrlArgPluginImpl();
  }

  @Override
  public ApiPlugin decode(JsonObject jsonObject) {
    if (!jsonObject.containsKey("url.arg")) {
      return null;
    }
    JsonArray jsonArray = jsonObject.getJsonArray("url.arg", new JsonArray());
    UrlArgPlugin urlArgPlugin = new UrlArgPluginImpl();
    for (int i = 0; i < jsonArray.size(); i++) {
      JsonObject parameterJson = jsonArray.getJsonObject(i);
      String name = parameterJson.getString("name");
      Preconditions.checkNotNull(name, "arg name cannot be null");
      Object defaultValue = parameterJson.getValue("default_value");
      Parameter parameter = Parameter.create(name, defaultValue);
      List<Rule> rules = RulesDecoder.instance().apply(parameterJson.getJsonObject("rules", new
              JsonObject()));
      rules.forEach(rule -> parameter.addRule(rule));
      urlArgPlugin.add(parameter);
    }
    return urlArgPlugin;


  }

  @Override
  public JsonObject encode(ApiPlugin plugin) {

    UrlArgPlugin urlArgPlugin = (UrlArgPlugin) plugin;

    return new JsonObject()
            .put("url.arg", createParamterArray(urlArgPlugin.parameters()));
  }

  private JsonArray createParamterArray(List<Parameter> parameters) {
    JsonArray jsonArray = new JsonArray();
    parameters.forEach(parameter -> {
      JsonObject jsonObject = new JsonObject()
              .put("name", parameter.name())
              .put("default_value", parameter.defaultValue());
      jsonArray.add(jsonObject);
      JsonObject rules = new JsonObject();
      jsonObject.put("rules", rules);
      parameter.rules().forEach(rule -> {
        rules.mergeIn(new JsonObject(rule.toMap()));
      });
    });
    return jsonArray;
  }
}
