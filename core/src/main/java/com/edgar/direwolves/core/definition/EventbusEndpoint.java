package com.edgar.direwolves.core.definition;

import io.vertx.core.json.JsonObject;

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
   * @return 消息头.
   */
  JsonObject header();

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
