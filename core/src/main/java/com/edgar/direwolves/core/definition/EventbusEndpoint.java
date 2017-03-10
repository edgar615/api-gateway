package com.edgar.direwolves.core.definition;

/**
 * 点对点的事件.
 *
 * @author Edgar  Date 2017/3/8
 */
public interface EventbusEndpoint extends Endpoint {
  String TYPE = "eventbus";

  String PUB_SUB = "pub-sub";

  String POINT_POINT = "point-point";

  String REQ_RESP = "req-resp";

  /**
   * @return 事件地址
   */
  String address();

  /**
   * @return 操作，方法，如果不为null，会在EventBus的消息头中增加 action : operation的头，用来匹配RPC调用.
   */
  String action();

  /**
   * 策略
   *
   * @return 三种策略：pub-sub、point-point、req-resp
   */
  String policy();


  default String type() {
    return TYPE;
  }
}
