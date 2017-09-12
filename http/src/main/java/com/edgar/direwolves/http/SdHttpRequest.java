package com.edgar.direwolves.http;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ArrayListMultimap;

import com.edgar.direwolves.core.rpc.RpcRequest;
import com.edgar.direwolves.core.rpc.http.HttpRpcRequest;
import io.vertx.servicediscovery.Record;

/**
 * 基于ServiceDiscovery的HTTP类型的rpc请求的定义.
 *
 * @author Edgar  Date 2016/12/26
 */
public class SdHttpRequest extends HttpRpcRequest {

  /**
   * 服务端信息
   */
  private Record record;

  public SdHttpRequest(String id, String name) {
    super(id, name);
  }

  @Override
  public int port() {
    return record.getLocation().getInteger("port");
  }

  @Override
  public String host() {
    return record.getLocation().getString("host");
  }

  /**
   * 创建HTTP类型的RPC请求
   *
   * @param id   id
   * @param name 名称
   * @return HttpRpcRequest
   */
  public static SdHttpRequest create(String id, String name) {
    return new SdHttpRequest(id, name);
  }


  @Override
  public String type() {
    return SdHttpEndpoint.TYPE;
  }

  @Override
  public RpcRequest copy() {
    SdHttpRequest copyReq = SdHttpRequest.create(id(), name());
    copyReq.setPath(path());
    if (record != null) {
      copyReq.setRecord(new Record(record.toJson()));
    } else {
      copyReq.setRecord(null);
    }
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

  public Record record() {
    return record;
  }

  public SdHttpRequest setRecord(Record record) {
    this.record = record;
    return this;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(SdHttpRequest.class.getSimpleName())
            .add("id", id())
            .add("name", name())
            .add("record", record.toJson())
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
    return record.getRegistration();
  }
}
