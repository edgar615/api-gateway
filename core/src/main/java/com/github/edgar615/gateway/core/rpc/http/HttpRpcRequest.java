package com.github.edgar615.gateway.core.rpc.http;

import com.github.edgar615.gateway.core.rpc.Fallbackable;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import com.github.edgar615.gateway.core.rpc.CircuitBreakerExecutable;
import com.github.edgar615.gateway.core.rpc.Fallbackable;
import com.github.edgar615.gateway.core.rpc.RpcRequest;
import com.github.edgar615.gateway.core.rpc.RpcResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/8/25.
 *
 * @author Edgar  Date 2017/8/25
 */
public abstract class HttpRpcRequest implements RpcRequest, CircuitBreakerExecutable, Fallbackable {
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
  private final String id;

  /**
   * 名称
   */
  private final String name;

  /**
   * 请求路径
   */
  private String path = "/";

  /**
   * HTTP方法
   */
  private HttpMethod httpMethod = HttpMethod.GET;

  /**
   * 请求体
   */
  private JsonObject body;

  /**
   * 请求超时时间，只有大于100的超时时间才有效
   */
  private int timeout = 10000;

  private RpcResponse fallback;

  protected HttpRpcRequest(String id, String name) {
    Preconditions.checkNotNull(id);
    Preconditions.checkNotNull(name);
    this.id = id;
    this.name = name;
  }

  public abstract int port();

  public abstract String host();

  @Override
  public String id() {
    return id;
  }

  @Override
  public String name() {
    return name;
  }

  public String path() {
    return path;
  }

  public HttpRpcRequest setPath(String path) {
    Preconditions.checkNotNull(path);
    this.path = path;
    return this;
  }

  public HttpMethod method() {
    return httpMethod;
  }

  public HttpRpcRequest setHttpMethod(HttpMethod httpMethod) {
    Preconditions.checkNotNull(httpMethod);
    this.httpMethod = httpMethod;
    return this;
  }

  public JsonObject body() {
    if (body == null) {
      return null;
    }
    return body.copy();
  }

  public HttpRpcRequest setBody(JsonObject body) {
    this.body = body;
    return this;
  }

  public int timeout() {
    return timeout;
  }

  public HttpRpcRequest setTimeout(int timeout) {
    this.timeout = timeout;
    return this;
  }

  public Multimap<String, String> params() {
    return ImmutableMultimap.copyOf(params);
  }

  public HttpRpcRequest addParam(String name, String value) {
    this.params.put(name, value);
    return this;
  }

  public HttpRpcRequest addParams(Multimap<String, String> params) {
    this.params.putAll(params);
    return this;
  }

  public HttpRpcRequest clearParams() {
    this.params.clear();
    return this;
  }

  public HttpRpcRequest clearHeaders() {
    this.headers.clear();
    return this;
  }

  public Multimap<String, String> headers() {
    return ImmutableMultimap.copyOf(headers);
  }

  public HttpRpcRequest addHeader(String name, String value) {
    this.headers.put(name, value);
    return this;
  }

  public HttpRpcRequest addHeaders(Multimap<String, String> header) {
    this.headers.putAll(header);
    return this;
  }

  public RpcResponse fallback() {
    return fallback;
  }

  @Override
  public void setFallback(RpcResponse fallback) {
    this.fallback = fallback;
  }

}
