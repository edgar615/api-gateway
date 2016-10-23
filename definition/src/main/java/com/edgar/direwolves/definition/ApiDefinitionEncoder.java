package com.edgar.direwolves.definition;

import com.edgar.direwolves.plugin.arg.Parameter;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.function.Function;

/**
 * 将ApiDefinition转换为JsonObject.
 * Created by edgar on 16-9-13.
 */
class ApiDefinitionEncoder implements Function<ApiDefinition, JsonObject> {

  private static final ApiDefinitionEncoder INSTANCE = new ApiDefinitionEncoder();

  private ApiDefinitionEncoder() {
  }

  static Function<ApiDefinition, JsonObject> instance() {
    return INSTANCE;
  }

  @Override
  public JsonObject apply(ApiDefinition definition) {
    return new JsonObject()
            .put("name", definition.name())
            .put("method", definition.method().name())
            .put("path", definition.path())
            .put("scope", definition.scope())
            .put("filters", definition.filters())
//            .put("whitelist", definition.whitelist())
//            .put("blacklist", definition.blacklist())
            .put("url_args", createParamterArray(definition.urlArgs()))
            .put("body_args", createParamterArray(definition.bodyArgs()))
            .put("endpoints", createEndpointArray(definition.endpoints()));

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

  private JsonArray createEndpointArray(List<Endpoint> endpoints) {
    JsonArray jsonArray = new JsonArray();
    endpoints.forEach(endpoint -> {
      if ("http".equals(endpoint.type())) {
        HttpEndpoint httpEndpoint = (HttpEndpoint) endpoint;
        jsonArray.add(httpEndpoint.toJson());
      }
    });
    return jsonArray;
  }

}
