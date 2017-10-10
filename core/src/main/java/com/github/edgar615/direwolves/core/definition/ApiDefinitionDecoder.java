package com.github.edgar615.direwolves.core.definition;

import com.google.common.base.Preconditions;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 将JsonObject转换为ApiDefinition.
 *
 * @author Edgar  Date 2016/9/13
 */
class ApiDefinitionDecoder implements Function<JsonObject, ApiDefinition> {
  private static final ApiDefinitionDecoder INSTANCE = new ApiDefinitionDecoder();

  private ApiDefinitionDecoder() {
  }

  static Function<JsonObject, ApiDefinition> instance() {
    return INSTANCE;
  }

  @Override
  public ApiDefinition apply(JsonObject jsonObject) {
    Preconditions.checkArgument(jsonObject.containsKey("name"), "api name cannot be null");
    Preconditions.checkArgument(jsonObject.containsKey("path"), "api path cannot be null");
    Preconditions
            .checkArgument(jsonObject.containsKey("endpoints"), "api endpoints cannot be null");
    String name = jsonObject.getString("name");
    String path = jsonObject.getString("path");
    HttpMethod method = method(jsonObject.getString("method", "get"));

    ApiDefinition apiDefinition = ApiDefinition
            .create(name, method, path, createEndpoints(jsonObject.getJsonArray("endpoints")));
    ApiPlugin.factories.forEach(f -> apiDefinition.addPlugin((ApiPlugin) f.decode(jsonObject)));
    return apiDefinition;
  }

  private HttpMethod method(String method) {
    HttpMethod httpMethod = HttpMethod.GET;
    if ("GET".equalsIgnoreCase(method)) {
      httpMethod = HttpMethod.GET;
    }
    if ("DELETE".equalsIgnoreCase(method)) {
      httpMethod = HttpMethod.DELETE;
    }
    if ("POST".equalsIgnoreCase(method)) {
      httpMethod = HttpMethod.POST;
    }
    if ("PUT".equalsIgnoreCase(method)) {
      httpMethod = HttpMethod.PUT;
    }
    return httpMethod;
  }

  private List<Endpoint> createEndpoints(JsonArray jsonArray) {
    List<Endpoint> endpoints = new ArrayList<>(jsonArray.size());
    for (int i = 0; i < jsonArray.size(); i++) {
      endpoints.add(Endpoints.fromJson(jsonArray.getJsonObject(i)));
    }
    return endpoints;
  }


}
