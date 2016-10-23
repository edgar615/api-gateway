package com.edgar.direwolves.definition;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2016/9/14.
 * 按照remove --> replace --> add的顺序执行
 *
 * @author Edgar  Date 2016/9/14
 */
public interface HttpEndpoint extends Endpoint {
  String TYPE = "http";

  /**
   * @return 名称，必填项，全局唯一.
   */
  String name();

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
