package com.edgar.direwolves.definition;

import com.google.common.base.Preconditions;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

import java.util.function.Function;

/**
 * Created by edgar on 16-9-24.
 */
class HttpEndpointDecoder implements Function<JsonObject, HttpEndpoint> {

  private static final HttpEndpointDecoder INSTANCE = new HttpEndpointDecoder();

  private HttpEndpointDecoder() {
  }

  static Function<JsonObject, HttpEndpoint> instance() {
    return INSTANCE;
  }

  @Override
  public HttpEndpoint apply(JsonObject jsonObject) {
    String type = jsonObject.getString("type");
    String name = jsonObject.getString("name");
    Preconditions.checkNotNull(name, "arg name cannot be null");
    String service = jsonObject.getString("service");
    Preconditions.checkNotNull(service, "arg service cannot be null");
    String path = jsonObject.getString("path");
    Preconditions.checkNotNull(path, "arg path cannot be null");
    if ("http".equalsIgnoreCase(type)) {
      HttpMethod method = HttpMethodDecoder.instance().apply(jsonObject);
      HttpEndpoint httpEndpoint = Endpoint.createHttp(name, method, path, service);
      return httpEndpoint;
    } else {
      throw new UnsupportedOperationException("unsupport type:" + type);
    }
  }

}
