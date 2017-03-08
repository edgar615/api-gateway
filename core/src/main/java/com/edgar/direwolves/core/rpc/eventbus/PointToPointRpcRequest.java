package com.edgar.direwolves.core.rpc.eventbus;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import com.edgar.direwolves.core.definition.PublishEndpoint;
import com.edgar.direwolves.core.rpc.RpcRequest;
import io.vertx.core.json.JsonObject;

/**
 * HTTP类型的rpc请求的定义.
 *
 * @author Edgar  Date 2016/12/26
 */
public class PointToPointRpcRequest implements RpcRequest {

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
   * 请求体
   */
  private final JsonObject message;

  PointToPointRpcRequest(String id, String name, String address, JsonObject message) {
    Preconditions.checkNotNull(id);
    Preconditions.checkNotNull(name);
    Preconditions.checkNotNull(address);
    Preconditions.checkNotNull(message);
    this.id = id;
    this.name = name;
    this.address = address;
    this.message = message;
  }

  /**
   * 创建HTTP类型的RPC请求
   *
   * @param id      id
   * @param name    名称
   * @param address 地址
   * @param message 消息内容
   * @return PointToPointRpcRequest
   */
  public static PointToPointRpcRequest create(String id, String name, String address,
                                              JsonObject message) {
    return new PointToPointRpcRequest(id, name, address, message);
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
    return PublishEndpoint.TYPE;
  }

  @Override
  public RpcRequest copy() {
    return PointToPointRpcRequest.create(id, name, address, message.copy());
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper("PointToPointRpcRequest")
            .add("id", id)
            .add("name", name)
            .add("address", address)
            .add("message", message.encode())
            .toString();
  }
}
