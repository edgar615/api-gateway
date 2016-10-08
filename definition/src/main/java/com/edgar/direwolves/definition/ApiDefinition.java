package com.edgar.direwolves.definition;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.regex.Pattern;

/**
 * API定义的接口.
 *
 * @author Edgar  Date 2016/9/13
 */
public interface ApiDefinition extends IpRestrictionDefinition, RateLimitDefinition {

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
   * 示例：/tasks，匹配请求：/tasks.
   * 示例：/tasks/([\\d+]+)/abandon，匹配请求/tasks/123/abandon
   *
   * @return API路径
   */
  String path();

  /**
   * @return 路径的正则表达式.在目前的设计中，它和path保持一致.
   */
  Pattern pattern();

  /**
   * @return 权限范围
   */
  String scope();

  /**
   * @return URL参数
   */
  List<Parameter> urlArgs();

  /**
   * @return body参数
   */
  List<Parameter> bodyArgs();

  /**
   * @return 远程请求定义
   */
  List<Endpoint> endpoints();

  /**
   * 返回filter的集合
   *
   * @return
   */
  List<String> filters();

  /**
   * 是否严格校验参数，如果该值为false，允许传入接口中未定义的参数，如果为true，禁止传入接口中未定义的参数.
   *
   * @return
   */
  boolean strictArg();

  /**
   * 新增一个filter
   *
   * @param filterType filter的类型
   */
  void addFilter(String filterType);

  /**
   * 删除一个filter
   *
   * @param filterType filter的类型
   */
  void removeFilter(String filterType);

  static ApiDefinition create(ApiDefinitionOption option) {
    return new ApiDefinitionImpl(option);
  }

  static ApiDefinition fromJson(JsonObject jsonObject) {
    return ApiDefinitionDecoder.instance().apply(jsonObject);
  }

  default JsonObject toJson() {
    return ApiDefinitionEncoder.instance().apply(this);
  }

}
