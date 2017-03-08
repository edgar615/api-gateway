package com.edgar.direwolves.core.definition;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import io.vertx.core.json.JsonObject;

/**
 * 点对点的事件.
 *
 * @author Edgar  Date 2017/3/8
 */
class PointToPointEndpointImpl implements PointToPointEndpoint {
  /**
   * 服务名
   */
  private final String name;

  /**
   * 事件地址
   */
  private String address;

  PointToPointEndpointImpl(String name, String address) {
    Preconditions.checkNotNull(name, "name can not be null");
    Preconditions.checkNotNull(address, "address can not be null");
    this.name = name;
    this.address = address;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper("PointToPointEndpoint")
            .add("name", name)
            .add("address", address)
            .toString();
  }

  @Override
  public String address() {
    return address;
  }

}
