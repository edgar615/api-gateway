package com.edgar.direwolves.core.definition;

/**
 * 广播事件.
 *
 * @author Edgar  Date 2017/3/8
 */
public interface PublishEndpoint extends Endpoint {
  String TYPE = "publish";

  /**
   * @return 事件地址
   */
  String address();

  default String type() {
    return TYPE;
  }
}
