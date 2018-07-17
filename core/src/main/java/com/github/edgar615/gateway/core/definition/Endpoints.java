package com.github.edgar615.gateway.core.definition;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * Created by Edgar on 2017/3/8.
 *
 * @author Edgar  Date 2017/3/8
 */
public class Endpoints {
  private static final List<EndpointCodec> codecList
          = Lists.newArrayList(ServiceLoader.load(EndpointCodec.class));

  public static Endpoint fromJson(JsonObject jsonObject) {
    String type = jsonObject.getString("type");
    Preconditions.checkNotNull(type, "endpoint type cannot be null");
    List<Endpoint> endpoints = codecList.stream()
            .filter(c -> type.equalsIgnoreCase(c.type()))
            .map(c -> c.fromJson(jsonObject))
            .collect(Collectors.toList());
    if (endpoints.isEmpty()) {
      throw new UnsupportedOperationException("unsupport endpoint type:" + type);
    }
    return endpoints.get(0);
  }

  public static JsonObject toJson(Endpoint endpoint) {
    String type = endpoint.type();
    List<JsonObject> endpoints = codecList.stream()
            .filter(c -> type.equalsIgnoreCase(c.type()))
            .map(c -> c.toJson(endpoint))
            .collect(Collectors.toList());
    if (endpoints.isEmpty()) {
      throw new UnsupportedOperationException("unsupport endpoint type:" + type);
    }
    return endpoints.get(0);
  }

  static HttpMethod method(String method) {
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
}
