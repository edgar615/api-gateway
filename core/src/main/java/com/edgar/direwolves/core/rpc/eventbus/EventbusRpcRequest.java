package com.edgar.direwolves.core.rpc.eventbus;

import com.edgar.direwolves.core.definition.EventbusEndpoint;
import com.edgar.direwolves.core.rpc.RpcRequest;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.vertx.core.json.JsonObject;

/**
 * Eventbus类型的rpc请求的定义.
 *
 * @author Edgar  Date 2016/12/26
 */
public class EventbusRpcRequest implements RpcRequest {

  /**
   * id
   */
  private final String id;

  /**
   * 名称
   */
  private final String name;

  /**
   * 服务端地址
   */
  private final String address;

  /**
   * 请求头
   */
  private Multimap<String, String> headers;

  /**
   * 请求体
   */
  private JsonObject message;

  /**
   * 三种策略：pub-sub、point-point、req-resp
   */
  private final String policy;

  EventbusRpcRequest(String id, String name, String address, String policy, Multimap<String, String> headers,
                     JsonObject message) {
    Preconditions.checkNotNull(id);
    Preconditions.checkNotNull(name);
    Preconditions.checkNotNull(address);
    Preconditions.checkNotNull(policy);
    Preconditions.checkNotNull(message);
    this.id = id;
    this.name = name;
    this.address = address;
    this.message = message;
    this.policy = policy;
    if (headers == null) {
      this.headers = ArrayListMultimap.create();
    } else {
      this.headers = ArrayListMultimap.create(headers);
    }
  }

  /**
   * 创建Eventbus类型的RPC请求
   *
   * @param id      id
   * @param name    名称
   * @param address 地址
   * @param headers  请求头
   * @param message 消息内容
   * @return EventbusRpcRequest
   */
  public static EventbusRpcRequest create(String id, String name, String address, String policy,
                                          Multimap<String, String> headers,
                                          JsonObject message) {
    return new EventbusRpcRequest(id, name, address, policy, headers, message);
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public String name() {
    return name;
  }

  public String address() {
    return address;
  }

  public JsonObject message() {
    return message.copy();
  }

  @Override
  public String type() {
    return EventbusEndpoint.TYPE;
  }

  public String policy() {
    return policy;
  }

  public EventbusRpcRequest clearHeaders() {
    this.headers.clear();
    return this;
  }

  public Multimap<String, String> headers() {
    return ImmutableMultimap.copyOf(headers);
  }

  public EventbusRpcRequest addHeader(String name, String value) {
    this.headers.put(name, value);
    return this;
  }

  public EventbusRpcRequest addHeaders(Multimap<String, String> header) {
    this.headers.putAll(header);
    return this;
  }
  public void addMessage(String key, Object value) {
    this.message.put(key, value);
  }

  public void replaceMessage(JsonObject jsonObject) {
    this.message.clear().mergeIn(jsonObject);
  }

  @Override
  public RpcRequest copy() {
    return EventbusRpcRequest.create(id, name, address, policy, headers, message.copy());
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper("EventbusRpcRequest")
        .add("id", id)
        .add("name", name)
        .add("address", address)
        .add("policy", policy)
        .add("headers", headers)
        .add("message", message.encode())
        .toString();
  }
}
