package com.edgar.direwolves.plugin.arg;

import com.edgar.direwolves.plugin.ApiPlugin;
import com.edgar.direwolves.plugin.ApiPluginFactory;
import com.edgar.util.validation.Rule;
import com.google.common.base.Preconditions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * Created by edgar on 16-10-22.
 */
public class UrlArgPluginFactory implements ApiPluginFactory<UrlArgPlugin> {

  @Override
  public String name() {
    return "URL_ARG";
  }

  @Override
  public ApiPlugin create() {
    return new UrlArgPluginImpl();
  }

  @Override
  public UrlArgPlugin decode(JsonObject jsonObject) {
//    Preconditions.checkArgument(jsonObject.containsKey("name"), "name cannot be null");
//    Preconditions.checkArgument("url_args".equalsIgnoreCase(jsonObject.getString("name")),
//        "name must be url_args");
//
//    Preconditions.checkArgument(jsonObject.containsKey("url_args"), "url_args cannot be null");
    JsonArray jsonArray = jsonObject.getJsonArray("url_arg", new JsonArray());
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
  public JsonObject encode(UrlArgPlugin plugin) {
    return new JsonObject()
        .put("url_arg", createParamterArray(plugin.parameters()));
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
