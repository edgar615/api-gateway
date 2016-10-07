package com.edgar.direwolves.definition;

import com.google.common.base.Preconditions;

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
    ApiDefinitionOption option = options(jsonObject);
    ApiDefinition apiDefinition = ApiDefinition.create(option);

    //filter
    filter(jsonObject, apiDefinition);
    //ip restriction
    ipRestriction(jsonObject, apiDefinition);

    //rate limit
    rateLimit(jsonObject, apiDefinition);
    return apiDefinition;
  }

  private ApiDefinitionOption options(JsonObject jsonObject) {
    ApiDefinitionOption option = new ApiDefinitionOption();
    option.setName(jsonObject.getString("name"));
    option.setPath(jsonObject.getString("path"));
    option.setScope(jsonObject.getString("scope", "default"));
    option.setMethod(HttpMethodDecoder.instance().apply(jsonObject));
    option.setStrictArg(jsonObject.getBoolean("strict_arg", false));
    if (jsonObject.containsKey("url_args")) {
      option.setUrlArgs(createParameterList(jsonObject.getJsonArray("url_args")));
    }
    if (jsonObject.containsKey("body_args")) {
      option.setBodyArgs(createParameterList(jsonObject.getJsonArray("body_args")));
    }
    option.setEndpoints(createEndpoints(jsonObject.getJsonArray("endpoints")));
    return option;
  }

  private void filter(JsonObject jsonObject, ApiDefinition apiDefinition) {
    JsonArray filters = jsonObject.getJsonArray("filters", new JsonArray());
    for (int i = 0; i < filters.size(); i++) {
      apiDefinition.addFilter(filters.getString(i));
    }
  }

  private void ipRestriction(JsonObject jsonObject, ApiDefinition apiDefinition) {
    JsonArray
            whiteArray = jsonObject.getJsonArray("whitelist", new JsonArray());
    JsonArray blackArray = jsonObject.getJsonArray("blacklist", new JsonArray());
    for (int i = 0; i < whiteArray.size(); i++) {
      apiDefinition.addWhitelist(whiteArray.getString(i));
    }
    for (int i = 0; i < blackArray.size(); i++) {
      apiDefinition.addBlacklist(blackArray.getString(i));
    }
  }

  private void rateLimit(JsonObject jsonObject, ApiDefinition apiDefinition) {
    JsonArray
            rateLimitArray = jsonObject.getJsonArray("rate_limit", new JsonArray());
    List<RateLimit> rateLimits = rateLimitDefinitions(rateLimitArray);
    rateLimits.forEach(rateLimitDefinition -> apiDefinition.addRateLimit(rateLimitDefinition));
  }

  private List<RateLimit> rateLimitDefinitions(JsonArray rateLimitArray) {
    List<RateLimit> definitions = new ArrayList<>();
    Function<JsonObject, RateLimit> decoder = RateLimitDecoder.instance();
    for (int i = 0; i < rateLimitArray.size(); i++) {
      definitions.add(decoder.apply(rateLimitArray.getJsonObject(i)));
    }
    return definitions;
  }

  private List<Parameter> createParameterList(JsonArray jsonArray) {
    List<Parameter> parameters = new ArrayList<>(jsonArray.size());
    Function<JsonObject, Parameter> decoder = ParameterDecoder.instance();
    for (int i = 0; i < jsonArray.size(); i++) {
      parameters.add(decoder.apply(jsonArray.getJsonObject(i)));
    }
    return parameters;
  }

  private List<Endpoint> createEndpoints(JsonArray endpoints) {
    List<Endpoint> httpEndpoints = new ArrayList<>(endpoints.size());
    for (int i = 0; i < endpoints.size(); i++) {
      httpEndpoints.add(HttpEndpoint.fromJson(endpoints.getJsonObject(i)));
    }
    return httpEndpoints;
  }


}
