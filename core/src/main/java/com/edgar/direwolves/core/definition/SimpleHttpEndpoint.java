package com.edgar.direwolves.core.definition;

import io.vertx.core.http.HttpMethod;

/**
 * Created by Edgar on 2016/9/14.
 * 最简单的HTTP类型的Endpoint。在endpoint中直接指定下游服务的host和port，
 * 不需要经过服务发现直接向下游服务转发
 *
 * @author Edgar  Date 2016/9/14
 */
public interface SimpleHttpEndpoint extends Endpoint, HttpEndpoint {
  String TYPE = "simple-http";

  /**
   * @return 下游服务的host.
   */
  String host();

  /**
   * 下游服务的端口
   *
   * @return
   */
  int port();

  /**
   * 创建HTTP类型的Endpoint
   *
   * @param name   名称
   * @param method 请求方法 GET | POST | DELETE | PUT
   * @param path   API路径
   * @param port   下游服务的端口
   * @param host   下游服务的host
   * @return SimpleHttpEndpoint
   */
  static SimpleHttpEndpoint http(String name, HttpMethod method, String path,
                                 int port, String host) {
    return new SimpleHttpEndpointImpl(name, method, path, port, host);
  }

  default String type() {
    return TYPE;
  }
}
