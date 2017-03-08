package com.edgar.direwolves.core.definition;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Created by Edgar on 2017/3/8.
 *
 * @author Edgar  Date 2017/3/8
 */
class ReqRespEndpointImpl implements ReqRespEndpoint {
  /**
   * 服务名
   */
  private final String name;

  /**
   * 事件地址
   */
  private final String address;

  /**
   * 操作，方法，如果不为null，会在EventBus的消息头中增加 action : operation的头，用来匹配RPC调用.
   */
  private final String action;

  ReqRespEndpointImpl(String name, String address, String action) {
    Preconditions.checkNotNull(name, "name can not be null");
    Preconditions.checkNotNull(address, "address can not be null");
    this.name = name;
    this.address = address;
    this.action = action;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String address() {
    return address;
  }

  @Override
  public String action() {
    return action;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper("ReqRespEndpoint")
            .add("name", name)
            .add("address", address)
            .add("action", action)
            .toString();
  }
}
