package com.edgar.direwolves.core.definition;

/**
 * 点对点的事件.
 *
 * @author Edgar  Date 2017/3/8
 */
public interface PointToPointEndpoint extends Endpoint {
  String TYPE = "point";

  /**
   * @return 事件地址
   */
  String address();

  default String type() {
    return TYPE;
  }
}
