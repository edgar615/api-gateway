package com.edgar.direwolves.definition;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2016/9/14.
 * 按照remove --> replace --> add的顺序执行
 *
 * @author Edgar  Date 2016/9/14
 */
public interface HttpEndpoint extends Endpoint, RequestTransformer {
  String TYPE = "http-endpoint";

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

  /**
   * 增加一个header
   *
   * @param key
   * @param value
   * @return
   */
  HttpEndpoint addReqHeader(String key, String value);

  /**
   * 替换一个header,只有当header存在时才替换;
   *
   * @param key
   * @param value
   * @return
   */
  HttpEndpoint replaceReqHeader(String key, String value);

  /**
   * 删除一个header
   *
   * @param key
   * @return
   */
  HttpEndpoint removeReqHeader(String key);

  /**
   * 增加一个url_arg
   *
   * @param key
   * @param value
   * @return
   */
  HttpEndpoint addReqUrlArg(String key, String value);

  /**
   * 替换一个url_arg,只有当url_arg存在时才替换;
   *
   * @param key
   * @param value
   * @return
   */
  HttpEndpoint replaceReqUrlArg(String key, String value);

  /**
   * 删除一个url_arg
   *
   * @param key
   * @return
   */
  HttpEndpoint removeReqUrlArg(String key);

  /**
   * 增加一个body_arg
   *
   * @param key
   * @param value
   * @return
   */
  HttpEndpoint addReqBodyArg(String key, String value);

  /**
   * 替换一个body_arg,只有当body_arg存在时才替换;
   *
   * @param key
   * @param value
   * @return
   */
  HttpEndpoint replaceReqBodyArg(String key, String value);

  /**
   * 删除一个body_arg
   *
   * @param key
   * @return
   */
  HttpEndpoint removeReqBodyArg(String key);

  static HttpEndpoint fromJson(JsonObject jsonObject) {
    return HttpEndpointDecoder.instance().apply(jsonObject);
  }

  default JsonObject toJson() {
    return HttpEndpointEncoder.instance().apply(this);
  }

  default String type() {
    return TYPE;
  }
}
