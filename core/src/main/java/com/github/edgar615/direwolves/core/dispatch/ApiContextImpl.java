package com.github.edgar615.direwolves.core.dispatch;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import com.github.edgar615.direwolves.core.definition.ApiDefinition;
import com.github.edgar615.direwolves.core.rpc.RpcRequest;
import com.github.edgar615.direwolves.core.rpc.RpcResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ApiContextImpl implements ApiContext {

  private final String path;

  private final HttpMethod method;

  private final Multimap<String, String> headers;

  private final Multimap<String, String> params;

  private final JsonObject body;

  private final Map<String, Object> variables = new HashMap<>();

  private final List<RpcRequest> requests = new ArrayList<>();

  private final List<RpcResponse> responses = new ArrayList<>();

  private final String id;

  private JsonObject principal;

  private ApiDefinition apiDefinition;

  private Result result;

  ApiContextImpl(String id, HttpMethod method, String path, Multimap<String, String> headers,
                 Multimap<String, String> params, JsonObject body) {
    this.id = id;
    this.path = path;
    this.method = method;
    if (headers == null) {
      headers = ArrayListMultimap.create();
    }
    this.headers = headers;
    if (params == null) {
      params = ArrayListMultimap.create();
    }
    this.params = params;
    this.body = body;
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public Multimap<String, String> params() {
    return ImmutableMultimap.copyOf(params);
  }

  @Override
  public Multimap<String, String> headers() {
    return ImmutableMultimap.copyOf(headers);
  }

  @Override
  public JsonObject body() {
    if (body != null) {
      return copyJson(body);
    }
    return null;
  }

  @Override
  public String path() {
    return path;
  }

  @Override
  public HttpMethod method() {
    return method;
  }

  @Override
  public JsonObject principal() {
    return principal;
  }

  @Override
  public void setPrincipal(JsonObject principal) {
    this.principal = principal;
  }

  @Override
  public Map<String, Object> variables() {
    return variables;
  }

  @Override
  public void addVariable(String name, Object value) {
    variables.put(name, value);
  }

  @Override
  public ApiDefinition apiDefinition() {
    return apiDefinition;
  }

  @Override
  public void setApiDefinition(ApiDefinition apiDefinition) {
    this.apiDefinition = apiDefinition;
  }

  @Override
  public List<RpcRequest> requests() {
    return requests;
  }

  @Override
  public void addRequest(RpcRequest request) {
    this.requests.add(request);
  }

  @Override
  public List<RpcResponse> responses() {
    return responses;
  }

  @Override
  public void addResponse(RpcResponse response) {
    this.responses.add(response);
  }

  @Override
  public Result result() {
    return result;
  }

  @Override
  public void setResult(Result result) {
    this.result = result;
  }

  @Override
  public String toString() {
    MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper("ApiContext")
            .add("id", id)
            .add("method", method)
            .add("path", path)
            .add("params", params)
            .add("headers", headers)
            .add("body", body)
            .add("variables", variables)
            .add("apiDefinition", apiDefinition);
    if (principal != null) {
      helper.add("principal", principal.encode());
    }
    helper.add("requests", requests);
    helper.add("responses", responses);
    helper.add("result", result);

    return helper.toString();
  }

  @Override
  public String nextRpcId() {
    int counter = (int) variables.getOrDefault("rpc.sn", 0) + 1;
    variables.put("rpc.sn", counter);
    return id + "." + counter;
  }

}