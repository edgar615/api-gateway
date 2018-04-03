package com.github.edgar615.direwolves.core.definition;

import com.google.common.base.Preconditions;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * API定义的接口.
 *
 * API名称约定的规范：[业务线].[应用名].[动作].[版本]
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
    if (!path.startsWith("/")) {
      path = "/" + path;
    }
    if (path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }
    return new ApiDefinitionImpl(name, method, path, endpoints, Pattern.compile(path));
  }

  static ApiDefinition createAnt(String name, HttpMethod method, String path,
                              List<Endpoint> endpoints) {
    return new AntPathApiDefinitionImpl(name, method, path, endpoints);
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
   * 是否是ant风格
   * @return
   */
  default boolean antStyle() {
    return this instanceof AntPathApiDefinitionImpl;
  }

  default JsonObject toJson() {
    return ApiDefinitionEncoder.instance().apply(this);
  }

  default boolean match(JsonObject filter) {
   return ApiDefinitionUtils.match(this, filter);
  }

}
