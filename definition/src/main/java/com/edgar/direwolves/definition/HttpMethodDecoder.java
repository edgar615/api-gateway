package com.edgar.direwolves.definition;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

import java.util.function.Function;

/**
 * 从JsonObject转换为HttpMethod对象
 *
 * @author Edgar  Date 2016/9/30
 */
class HttpMethodDecoder implements Function<JsonObject, HttpMethod> {

  private static final HttpMethodDecoder INSTANCE = new HttpMethodDecoder();

  private HttpMethodDecoder() {
  }

  static Function<JsonObject, HttpMethod> instance() {
    return INSTANCE;
  }

  @Override
  public HttpMethod apply(JsonObject jsonObject) {
    HttpMethod httpMethod = HttpMethod.GET;
    String method = jsonObject.getString("method", "GET");
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
