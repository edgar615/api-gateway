package com.edgar.direwolves.core.rpc.http;

import com.edgar.direwolves.core.definition.HttpEndpoint;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.rpc.RpcRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

import java.util.UUID;

/**
 * HTTP类型的rpc请求的定义.
 *
 * @author Edgar  Date 2016/12/26
 */
public class HttpRpcRequest implements RpcRequest {

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
   * 服务端端口
   */
  private int port = 80;

  /**
   * 服务端的ID
   */
  private String serverId = "undefined";

  /**
   * 服务端地址
   */
  private String host = "localhost";

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

  protected HttpRpcRequest(String id, String name) {
    Preconditions.checkNotNull(id);
    Preconditions.checkNotNull(name);
    this.id = id;
    this.name = name;
  }

  /**
   * 创建HTTP类型的RPC请求
   * @param id id
   * @param name 名称
   * @return HttpRpcRequest
   */
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
    return HttpEndpoint.TYPE;
  }

  @Override
  public RpcRequest copy() {
    HttpRpcRequest copyReq = HttpRpcRequest.create(id, name);
    copyReq.setPath(path);
    copyReq.setPort(port);
    copyReq.setHost(host);
    copyReq.setHttpMethod(httpMethod);
    copyReq.setTimeout(timeout);
    copyReq.setBody(body());
    copyReq.addParams(ArrayListMultimap.create(params));
    copyReq.addHeaders(ArrayListMultimap.create(headers));
    copyReq.setServerId(serverId);
    return copyReq;
  }

  public String serverId() {
    return serverId;
  }

  public void setServerId(String serverId) {
    this.serverId = serverId;
  }

  public int port() {
    return port;
  }

  public HttpRpcRequest setPort(int port) {
    this.port = port;
    return this;
  }

  public String host() {
    return host;
  }

  public HttpRpcRequest setHost(String host) {
    Preconditions.checkNotNull(host);
    this.host = host;
    return this;
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

  @Override
  public String toString() {
    return MoreObjects.toStringHelper("HttpRpcRequest")
            .add("serverId", serverId)
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
