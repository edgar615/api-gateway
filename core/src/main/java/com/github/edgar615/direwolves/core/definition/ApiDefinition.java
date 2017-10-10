package com.github.edgar615.direwolves.core.definition;

import com.google.common.base.Preconditions;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * API定义的接口.
 *
 * @author Edgar  Date 2016/9/13
 */
public interface ApiDefinition {

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
   * @return 远程请求定义
   */
  List<Endpoint> endpoints();

  /**
   * @return 插件列表
   */
  List<ApiPlugin> plugins();

  /**
   * 增加一个插件.同一个名字的插件有且只能有一个，后加入的插件会覆盖掉之前的同名插件
   *
   * @param plugin 插件
   * @return ApiDefinition
   */
  ApiDefinition addPlugin(ApiPlugin plugin);

  /**
   * 删除一个插件.
   *
   * @param name 插件名称
   * @return ApiDefinition
   */
  ApiDefinition removePlugin(String name);

  static ApiDefinition create(String name, HttpMethod method, String path,
                              List<Endpoint> endpoints) {
    return new ApiDefinitionImpl(name, method, path, endpoints);
  }

  static ApiDefinition fromJson(JsonObject jsonObject) {
    return ApiDefinitionDecoder.instance().apply(jsonObject);
  }

  /**
   * 根据插件名称返回插件
   *
   * @param name 插件名称
   * @return 如果未找到对应的插件，返回null;
   */
  default ApiPlugin plugin(String name) {
    Preconditions.checkNotNull(name, "name cannot be null");
    List<ApiPlugin> apiPlugins =
            plugins().stream()
                    .filter(p -> name.equalsIgnoreCase(p.name()))
                    .collect(Collectors.toList());
    if (apiPlugins.isEmpty()) {
      return null;
    }
    return apiPlugins.get(0);
  }

  /**
   * 校验传入的参数是否符合api定义.
   * 只有当method相同，且path符合ApiDefinition的正则表达式才认为二者匹配.
   *
   * @param method 请求方法
   * @param path   路径
   * @return true 符合
   */
  @Deprecated
  default boolean match(HttpMethod method, String path) {
    if (method != method()) {
      return false;
    }
    Pattern pattern = pattern();
    Matcher matcher = pattern.matcher(path);
    return matcher.matches();
  }

  default JsonObject toJson() {
    return ApiDefinitionEncoder.instance().apply(this);
  }

  /**
   * 对性能有损耗
   * @return
   */
  @Deprecated
  default ApiDefinition copy() {
    JsonObject jsonObject = toJson();
    return ApiDefinition.fromJson(jsonObject);
  }

  default boolean match(JsonObject filter) {
   return ApiDefinitionUtils.match(this, filter);
  }

}
