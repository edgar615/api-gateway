package com.github.edgar615.direwolves.core.rpc.dummy;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import com.github.edgar615.direwolves.core.definition.DummyEndpoint;
import com.github.edgar615.direwolves.core.rpc.RpcRequest;
import io.vertx.core.json.JsonObject;

/**
 * Dummy类型的rpc请求的定义.
 *
 * @author Edgar  Date 2016/12/26
 */
public class DummyRequest implements RpcRequest {

  /**
   * id
   */
  private final String id;

  /**
   * 名称
   */
  private final String name;

  /**
   * 结果.
   */
  private final JsonObject result;

  DummyRequest(String id, String name, JsonObject result) {
    Preconditions.checkNotNull(id);
    Preconditions.checkNotNull(name);
    this.id = id;
    this.name = name;
    if (result == null) {
      this.result = new JsonObject();
    } else {
      this.result = result;
    }
  }

  /**
   * 创建Eventbus类型的RPC请求
   *
   * @param id      id
   * @param name    名称
   * @param result 结果
   * @return DummyRequest
   */
  public static DummyRequest create(String id, String name, JsonObject result) {
    return new DummyRequest(id, name, result);
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public String name() {
    return name;
  }

  public JsonObject result() {
    return result.copy();
  }

  @Override
  public String type() {
    return DummyEndpoint.TYPE;
  }

  @Override
  public RpcRequest copy() {
    return new DummyRequest(id, name, result.copy());
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper("DummyRequest")
        .add("id", id)
        .add("name", name)
        .add("result", result.encode())
        .toString();
  }
}
