package com.github.edgar615.gateway.http;

import com.google.common.base.Preconditions;

import com.github.edgar615.gateway.core.definition.Endpoint;
import com.github.edgar615.gateway.core.definition.EndpointCodec;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/3/8.
 *
 * @author Edgar  Date 2017/3/8
 */
public class SdHttpEndpointCodec implements EndpointCodec {

  @Override
  public Endpoint fromJson(JsonObject jsonObject) {
    String type = jsonObject.getString("type");
    Preconditions.checkNotNull(type, "endpoint type cannot be null");
    Preconditions.checkArgument(type.equalsIgnoreCase("http"),
                                "endpoint name must be http");
    String name = jsonObject.getString("name", "default");
    Preconditions.checkNotNull(name, "endpoint name cannot be null");
    String service = jsonObject.getString("service");
    Preconditions.checkNotNull(service, "endpoint service cannot be null");
    String path = jsonObject.getString("path");
    Preconditions.checkNotNull(path, "endpoint path cannot be null");
    HttpMethod method = transferMethod(jsonObject.getString("method", "get"));
    return new SdHttpEndpointImpl(name, method, path, service);
  }

  @Override
  public JsonObject toJson(Endpoint endpoint) {
    SdHttpEndpoint httpEndpoint = (SdHttpEndpoint) endpoint;
    return new JsonObject()
            .put("type", httpEndpoint.type())
            .put("name", httpEndpoint.name())
            .put("service", httpEndpoint.service())
            .put("path", httpEndpoint.path())
            .put("method", httpEndpoint.method());
  }

  @Override
  public String type() {
    return SdHttpEndpoint.TYPE;
  }

  private HttpMethod transferMethod(String method) {
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
