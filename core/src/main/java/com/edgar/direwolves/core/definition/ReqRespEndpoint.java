package com.edgar.direwolves.core.definition;

/**
 * 请求/回应事件.
 *
 * @author Edgar  Date 2017/3/8
 */
public interface ReqRespEndpoint extends Endpoint {
  String TYPE = "req-resp";

  /**
   * @return 事件地址
   */
  String address();

  /**
   * @return 操作，方法，如果不为null，会在EventBus的消息头中增加 action : operation的头，用来匹配RPC调用.
   */
  String action();

  default String type() {
    return TYPE;
  }
}
