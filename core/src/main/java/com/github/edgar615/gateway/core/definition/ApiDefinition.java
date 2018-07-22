package com.github.edgar615.gateway.core.definition;

import com.google.common.base.Preconditions;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * API定义的接口.
 * <p>
 * API名称约定的规范：[业务线].[应用名].[动作].[版本]
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
   * 示例：/tasks/123/abandon，匹配请求/tasks/123/abandon
   *
   * @return API路径
   */
  String path();

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
    return new ApiDefinitionImpl(name, method, path, endpoints);
  }

  static ApiDefinition createAnt(String name, HttpMethod method, String path,
                                 List<Endpoint> endpoints) {
    return new AntPathApiDefinition(name, method, path, endpoints);
  }

  static ApiDefinition createRegex(String name, HttpMethod method, String path,
                                   List<Endpoint> endpoints) {
    return new RegexPathApiDefinition(name, method, path, endpoints);
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
   *
   * @return
   */
  default boolean antStyle() {
    return this instanceof AntPathApiDefinition;
  }

  /**
   * 是否是regex风格
   *
   * @return
   */
  default boolean regexStyle() {
    return this instanceof RegexPathApiDefinition;
  }

  default JsonObject toJson() {
    return ApiDefinitionEncoder.instance().apply(this);
  }

  default boolean match(JsonObject filter) {
    return ApiDefinitionUtils.match(this, filter);
  }

  /**
   * 按照 相等>正则>ant的优先级匹配
   *
   * @param apiDefinitions
   * @return
   */
  static List<ApiDefinition> extractInOrder(List<ApiDefinition> apiDefinitions) {
    if (apiDefinitions.size() <= 1) {//只有一个
      return apiDefinitions;
    }
    //先判断相等
    List<ApiDefinition> apiList = apiDefinitions.stream()
            .filter(d -> !d.antStyle() && !d.regexStyle())
            .collect(Collectors.toList());
    if (!apiList.isEmpty()) {
      return apiList;
    }
    //判断正则
    //优先选择正则匹配的API
    List<ApiDefinition> regexApiList = apiDefinitions.stream()
            .filter(d -> d.regexStyle())
            .collect(Collectors.toList());
    if (!regexApiList.isEmpty()) {
      return regexApiList;
    }
    List<ApiDefinition> antApiList = apiDefinitions.stream()
            .filter(d -> d.antStyle())
            .collect(Collectors.toList());
    return antApiList;
  }
}
