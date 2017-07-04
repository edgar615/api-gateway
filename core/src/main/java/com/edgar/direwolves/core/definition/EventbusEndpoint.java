package com.edgar.direwolves.core.definition;

import com.google.common.collect.Multimap;

/**
 * Eventbus的Endpoint.
 *
 * @author Edgar  Date 2017/3/8
 */
public interface EventbusEndpoint extends Endpoint {
  String TYPE = "eventbus";

  String PUB_SUB = "pub-sub";

  String POINT_POINT = "point-point";

  String REQ_RESP = "req-resp";

  String action();

  /**
   * @return 事件地址
   */
  String address();

  /**
   * @return 消息头.
   */
  Multimap<String, String> headers();

  /**
   * 策略
   *
   * @return 三种策略：pub-sub、point-point、req-resp
   */
  String policy();

  /**
   * 创建Req-Resp类型的Endpoint
   *
   * @param name    名称
   * @param address 事件地址
   * @param header  请求头
   * @return
   */
  static EventbusEndpoint reqResp(String name, String address, String action,
                                  Multimap<String, String> header) {
    return new EventbusEndpointImpl(name, address, EventbusEndpoint.REQ_RESP, action, header);
  }

  /**
   * 创建广播类型的Endpoint
   *
   * @param name    名称
   * @param address 事件地址
   * @param header  请求头
   * @return
   */
  static EventbusEndpoint publish(String name, String address, String action,
                                  Multimap<String, String> header) {
    return new EventbusEndpointImpl(name, address, EventbusEndpoint.PUB_SUB, action, header);
  }

  /**
   * 创建点对点类型的Endpoint
   *
   * @param name    名称
   * @param address 事件地址
   * @param header  请求头
   * @return
   */
  static EventbusEndpoint pointToPoint(String name, String address, String action,
                                       Multimap<String, String> header) {
    return new EventbusEndpointImpl(name, address, EventbusEndpoint.POINT_POINT, action, header);
  }

  default String type() {
    return TYPE;
  }
}
