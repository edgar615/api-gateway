package com.edgar.direwolves.core.rpc;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

/**
 * HTTP类型的rpc请求定义.
 *
 * @author Edgar  Date 2016/12/26
 */
public class HttpRpcRequest implements RpcRequest {

  private final Multimap<String, String> headers = ArrayListMultimap.create();

  private final Multimap<String, String> params = ArrayListMultimap.create();

  private final String id;

  private final String name;

  private int port = 80;

  private String host = "localhost";

  private String path = "/";

  private HttpMethod httpMethod = HttpMethod.GET;

  private JsonObject body;

  private int timeout = 10000;

  HttpRpcRequest(String id, String name) {
    this.id = id;
    this.name = name;
  }

  public static HttpRpcRequest create(String id, String name) {
    return new HttpRpcRequest(id, name);
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String type() {
    return "HTTP";
  }

  @Override
  public RpcRequest copy() {
    HttpRpcRequest copyReq = HttpRpcRequest.create(id, name);
    copyReq.setPort(port);
    copyReq.setHost(host);
    copyReq.setHttpMethod(httpMethod);
    copyReq.setTimeout(timeout);
    copyReq.setBody(getBody());
    copyReq.addParams(getParams());
    copyReq.addHeaders(getHeaders());
    return copyReq;
  }

  public String getName() {
    return name;
  }

  public int getPort() {
    return port;
  }

  public HttpRpcRequest setPort(int port) {
    this.port = port;
    return this;
  }

  public String getHost() {
    return host;
  }

  public HttpRpcRequest setHost(String host) {
    Preconditions.checkNotNull(host);
    this.host = host;
    return this;
  }

  public String getPath() {
    return path;
  }

  public HttpRpcRequest setPath(String path) {
    Preconditions.checkNotNull(path);
    this.path = path;
    return this;
  }

  public HttpMethod getHttpMethod() {
    return httpMethod;
  }

  public HttpRpcRequest setHttpMethod(HttpMethod httpMethod) {
    Preconditions.checkNotNull(httpMethod);
    this.httpMethod = httpMethod;
    return this;
  }

  public JsonObject getBody() {
    if (body == null) {
      return null;
    }
    return body.copy();
  }

  public HttpRpcRequest setBody(JsonObject body) {
    this.body = body;
    return this;
  }

  public int getTimeout() {
    return timeout;
  }

  public HttpRpcRequest setTimeout(int timeout) {
    this.timeout = timeout;
    return this;
  }

  public Multimap<String, String> getParams() {
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

  public Multimap<String, String> getHeaders() {
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

  public String getId() {
    return id;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper("HttpRpcRequest")
        .add("id", id)
        .add("name", name)
        .add("host", host)
        .add("port", port)
        .add("method", httpMethod)
        .add("path", path)
        .add("timeout", timeout)
        .add("headers", headers)
        .add("params", params)
        .add("body", body == null ? null : body.encode())
        .toString();
  }
}
