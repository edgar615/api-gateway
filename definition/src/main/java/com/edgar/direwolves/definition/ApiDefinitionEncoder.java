package com.edgar.direwolves.definition;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.function.Function;

/**
 * 将ApiDefinition转换为JsonObject.
 * Created by edgar on 16-9-13.
 */
public class ApiDefinitionEncoder implements Function<ApiDefinition, JsonObject> {

  private static final ApiDefinitionEncoder INSTANCE = new ApiDefinitionEncoder();

  private ApiDefinitionEncoder() {
  }

  public static Function<ApiDefinition, JsonObject> instance() {
    return INSTANCE;
  }

  @Override
  public JsonObject apply(ApiDefinition definition) {
    JsonArray rateLimtArray = new JsonArray();
    for (RateLimit rateLimit : definition.rateLimits()) {
      JsonObject jsonObject = new JsonObject()
              .put("limit", rateLimit.limit())
              .put("limit_by", rateLimit.limitBy())
              .put("type", rateLimit.type());
      rateLimtArray.add(jsonObject);
    }
    return new JsonObject()
            .put("name", definition.name())
            .put("method", definition.method().name())
            .put("path", definition.path())
            .put("scope", definition.scope())
            .put("filters", definition.filters())
            .put("whitelist", definition.whitelist())
            .put("blacklist", definition.blacklist())
            .put("rate_limit", rateLimtArray)
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
      JsonArray ruleArray = new JsonArray();
      jsonObject.put("rules", ruleArray);
      parameter.rules().forEach(rule -> {
        ruleArray.add(new JsonObject(rule.toMap()));
      });
    });
    return jsonArray;
  }

  private JsonArray createEndpointArray(List<Endpoint> endpoints) {
    JsonArray jsonArray = new JsonArray();
    endpoints.forEach(endpoint -> {
      if ("http-endpoint".equals(endpoint.type())) {
        HttpEndpoint httpEndpoint = (HttpEndpoint) endpoint;
        JsonObject jsonObject = new JsonObject()
                .put("name", httpEndpoint.name())
                .put("method", httpEndpoint.method())
                .put("path", httpEndpoint.path())
                .put("service", httpEndpoint.service());
        jsonArray.add(jsonObject);
      }
    });
    return jsonArray;
  }

}
