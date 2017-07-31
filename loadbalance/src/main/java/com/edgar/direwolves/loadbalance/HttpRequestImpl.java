package com.edgar.direwolves.loadbalance;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;

/**
 * Created by Edgar on 2017/7/31.
 *
 * @author Edgar  Date 2017/7/31
 */
public class HttpRequestImpl implements HttpRequest {
  /**
   * 请求头
   */
  private final Multimap<String, String> headers = ArrayListMultimap.create();

  /**
   * 请求参数
   */
  private final Multimap<String, String> params = ArrayListMultimap.create();

  /**
   * 下游服务
   */
  private final Record record;

  /**
   * HTTP方法
   */
  private final HttpMethod httpMethod;

  /**
   * 请求体
   */
  private final JsonObject body;

  /**
   * 请求超时时间，默认10秒
   */
  private int timeout = 10000;

  /**
   * 请求地址
   */
  private final String path;

  /**
   * ID
   */
  private final String id;

  public HttpRequestImpl(String id,
                         Record record,
                         HttpMethod httpMethod,
                         String path,
                         JsonObject body) {
    this.id = id;
    this.record = record;
    this.httpMethod = httpMethod;
    this.path = path;
    this.body = body;
  }

//  protected HttpRequestImpl(String service) {
//    Preconditions.checkNotNull(id);
//    Preconditions.checkNotNull(name);
//    this.id = id;
//    this.name = name;
//  }
//
//  /**
//   * 创建HTTP类型的RPC请求
//   * @param id id
//   * @param name 名称
//   * @return HttpRequestImpl
//   */
//  public static HttpRpcRequest create(String id, String name) {
//    return new HttpRpcRequest(id, name);
//  }
//
//  @Override
//  public String id() {
//    return id;
//  }
//
//  @Override
//  public String name() {
//    return name;
//  }
//
//  @Override
//  public String type() {
//    return HttpEndpoint.TYPE;
//  }
//
//  @Override
//  public RpcRequest copy() {
//    HttpRpcRequest copyReq = HttpRpcRequest.create(id, name);
//    copyReq.setPath(path);
//    copyReq.setPort(port);
//    copyReq.setHost(host);
//    copyReq.setHttpMethod(method);
//    copyReq.setTimeout(timeout);
//    copyReq.setBody(body());
//    copyReq.addParams(ArrayListMultimap.create(params));
//    copyReq.addHeaders(ArrayListMultimap.create(headers));
//    copyReq.setServerId(serverId);
//    return copyReq;
//  }


  @Override
  public String id() {
    return id;
  }

  @Override
  public String path() {
    return path;
  }

  @Override
  public int timeout() {
    return timeout;
  }

  @Override
  public JsonObject body() {
    return body;
  }

  @Override
  public HttpMethod method() {
    return httpMethod;
  }

  @Override
  public Record record() {
    return record;
  }

  @Override
  public Multimap<String, String> params() {
    return ImmutableMultimap.copyOf(params);
  }

  @Override
  public Multimap<String, String> headers() {
    return ImmutableMultimap.copyOf(headers);
  }

  HttpRequest setTimeout(int timeout) {
    this.timeout = timeout;
    return this;
  }

  HttpRequest addParam(String name, String value) {
    this.params.put(name, value);
    return this;
  }

  HttpRequest addHeader(String name, String value) {
    this.headers.put(name, value);
    return this;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper("HttpRequestImpl")
            .add("id", id)
            .add("record", record.toJson())
            .add("method", httpMethod)
            .add("path", path)
            .add("timeout", timeout)
            .add("headers", headers)
            .add("params", params)
            .add("body", body == null ? null : body.encode())
            .toString();
  }
}
