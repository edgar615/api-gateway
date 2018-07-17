package com.github.edgar615.gateway.core.rpc.http;

import com.github.edgar615.gateway.core.definition.SimpleHttpEndpoint;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;

import com.github.edgar615.gateway.core.definition.SimpleHttpEndpoint;
import com.github.edgar615.gateway.core.rpc.RpcRequest;

/**
 * HTTP类型的rpc请求的定义.
 *
 * @author Edgar  Date 2016/12/26
 */
public class SimpleHttpRequest extends HttpRpcRequest {

  /**
   * 服务端端口
   */
  private int port = 80;

  /**
   * 服务端地址
   */
  private String host = "localhost";

  SimpleHttpRequest(String id, String name) {
    super(id, name);
  }

  /**
   * 创建HTTP类型的RPC请求
   *
   * @param id   id
   * @param name 名称
   * @return HttpRpcRequest
   */
  public static SimpleHttpRequest create(String id, String name) {
    return new SimpleHttpRequest(id, name);
  }

  @Override
  public String type() {
    return SimpleHttpEndpoint.TYPE;
  }

  @Override
  public RpcRequest copy() {
    SimpleHttpRequest copyReq = SimpleHttpRequest.create(id(), name());
    copyReq.setPath(path());
    copyReq.setPort(port);
    copyReq.setHost(host);
    copyReq.setHttpMethod(method());
    copyReq.setTimeout(timeout());
    copyReq.setBody(body());
    copyReq.addParams(ArrayListMultimap.create(params()));
    copyReq.addHeaders(ArrayListMultimap.create(headers()));
    if (fallback() != null) {
      copyReq.setFallback(fallback().copy());
    }
    return copyReq;
  }

  @Override
  public int port() {
    return port;
  }

  public SimpleHttpRequest setPort(int port) {
    this.port = port;
    return this;
  }

  @Override
  public String host() {
    return host;
  }

  public SimpleHttpRequest setHost(String host) {
    Preconditions.checkNotNull(host);
    this.host = host;
    return this;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(SimpleHttpRequest.class.getSimpleName())
            .add("id", id())
            .add("name", name())
            .add("host", host)
            .add("port", port)
            .add("method", method())
            .add("path", path())
            .add("timeout", timeout())
            .add("headers", headers())
            .add("params", params())
            .add("body", body() == null ? null : body().encode())
            .add("fallback", fallback())
            .toString();
  }

  @Override
  public String circuitBreakerName() {
    return host + ":" + port;
  }
}
