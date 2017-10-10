package com.github.edgar615.direwolves.http;

import com.github.edgar615.direwolves.core.definition.HttpEndpoint;
import io.vertx.core.http.HttpMethod;

/**
 * Created by Edgar on 2016/9/14.
 * 基于服务发现的HTTP调用
 *
 * @author Edgar  Date 2016/9/14
 */
public interface SdHttpEndpoint extends HttpEndpoint {
  String TYPE = "http";

  /**
   * 创建HTTP类型的Endpoint
   *
   * @param name    名称
   * @param method  请求方法 GET | POST | DELETE | PUT
   * @param path    API路径
   * @param service 服务名，用于服务发现
   * @return SdHttpEndpoint
   */
  static SdHttpEndpoint http(String name, HttpMethod method, String path, String service) {
    return new SdHttpEndpointImpl(name, method, path, service);
  }

  /**
   * @return 请求方法 GET | POST | DELETE | PUT.
   */
  HttpMethod method();

  /**
   * API路径
   * 示例：/tasks，匹配请求：/tasks.
   * 示例：/tasks/$param1，匹配请求：/tasks/变量param1.
   *
   * @return API路径
   */
  String path();

  /**
   * @return 服务名，用于服务发现.
   */
  String service();

  default String type() {
    return TYPE;
  }
}
