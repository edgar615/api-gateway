package com.edgar.direwolves.http.loadbalance;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

import java.util.UUID;

/**
 * Created by Edgar on 2017/7/31.
 *
 * @author Edgar  Date 2017/7/31
 */
@Deprecated
public class HttpRequestBuilder {
  /**
   * 请求头
   */
  private final Multimap<String, String> headers = ArrayListMultimap.create();

  /**
   * 请求参数
   */
  private final Multimap<String, String> params = ArrayListMultimap.create();

  /**
   * id
   */
  private String service;

  /**
   * HTTP方法
   */
  private HttpMethod httpMethod = HttpMethod.GET;

  /**
   * 请求体
   */
  private JsonObject body;

  /**
   * 请求超时时间
   */
  private int timeout = 10000;

  private String id = UUID.randomUUID().toString();

  public Multimap<String, String> getHeaders() {
    return headers;
  }

  public Multimap<String, String> getParams() {
    return params;
  }

  public String getId() {
    return id;
  }

  public HttpRequestBuilder setId(String id) {
    this.id = id;
    return this;
  }

  public String getService() {
    return service;
  }

  public HttpRequestBuilder setService(String service) {
    this.service = service;
    return this;
  }

  public HttpMethod getHttpMethod() {
    return httpMethod;
  }

  public HttpRequestBuilder setHttpMethod(HttpMethod httpMethod) {
    this.httpMethod = httpMethod;
    return this;
  }

  public JsonObject getBody() {
    return body;
  }

  public HttpRequestBuilder setBody(JsonObject body) {
    this.body = body;
    return this;
  }

  public int getTimeout() {
    return timeout;
  }

  public HttpRequestBuilder setTimeout(int timeout) {
    this.timeout = timeout;
    return this;
  }

  public HttpRequestBuilder addParam(String name, String value) {
    this.params.put(name, value);
    return this;
  }

  public HttpRequestBuilder addHeader(String name, String value) {
    this.headers.put(name, value);
    return this;
  }
}
