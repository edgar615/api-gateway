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
public class ReqRespRpcRequest implements RpcRequest {

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
   * 服务端地址
   */
  private final String action;

  /**
   * 请求体
   */
  private final JsonObject message;

  ReqRespRpcRequest(String id, String name, String address, String action, JsonObject message) {
    this.action = action;
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
   * @param action  操作
   * @param message 消息内容
   * @return ReqRespRpcRequest
   */
  public static ReqRespRpcRequest create(String id, String name, String address, String action,
                                         JsonObject message) {
    return new ReqRespRpcRequest(id, name, address, action, message);
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

  public String action() {
    return action;
  }

  @Override
  public String type() {
    return PublishEndpoint.TYPE;
  }

  @Override
  public RpcRequest copy() {
    return ReqRespRpcRequest.create(id, name, address, action, message.copy());
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper("PointToPointRpcRequest")
            .add("id", id)
            .add("name", name)
            .add("address", address)
            .add("action", action)
            .add("message", message.encode())
            .toString();
  }
}
