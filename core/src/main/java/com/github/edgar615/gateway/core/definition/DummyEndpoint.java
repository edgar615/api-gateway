package com.github.edgar615.gateway.core.definition;

import io.vertx.core.json.JsonObject;

/**
 * 不做远程调用的endpoint，它会直接返回一个JSON对象.
 *
 * @author Edgar  Date 2017/3/8
 */
public interface DummyEndpoint extends Endpoint {
  String TYPE = "dummy";

  /**
   * 创建一个Dummy类型的endpoint
   *
   * @param name   名称
   * @param result 　响应结果
   * @return
   */
  static DummyEndpoint dummy(String name, JsonObject result) {
    return new DummyEndpointImpl(name, result);
  }

  JsonObject result();

  default String type() {
    return TYPE;
  }
}
