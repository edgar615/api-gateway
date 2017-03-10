package com.edgar.direwolves.core.definition;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;

/**
 * Created by Edgar on 2017/3/8.
 *
 * @author Edgar  Date 2017/3/8
 */
class EventbusEndpointImpl implements EventbusEndpoint {
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

  /**
   * 三种策略：pub-sub、point-point、req-resp
   */
  private final String policy;

  EventbusEndpointImpl(String name, String address, String policy, String action) {
    Preconditions.checkNotNull(name, "name can not be null");
    Preconditions.checkNotNull(address, "address can not be null");
    Preconditions.checkNotNull(policy, "policy can not be null");
    if (!"pub-sub".equalsIgnoreCase(policy)
            && !"point-point".equalsIgnoreCase(policy)
            && !"req-resp".equalsIgnoreCase(policy)) {
      throw SystemException.create(DefaultErrorCode.INVALID_ARGS)
              .set("details", "policy must be pub-sub | point-point | req-resp");
    }
    this.name = name;
    this.address = address;
    this.policy = policy;
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
  public String policy() {
    return policy;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper("EventbusEndpoint")
            .add("name", name)
            .add("address", address)
            .add("policy", policy)
            .add("action", action)
            .toString();
  }
}
