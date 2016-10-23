package com.edgar.direwolves.definition;

import com.edgar.direwolves.plugin.arg.Parameter;
import com.edgar.direwolves.plugin.transformer.ResponseTransformer;
import com.google.common.base.Preconditions;

import com.edgar.direwolves.plugin.ApiPlugin;
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

  /**
   * 删除所有filter
   */
  void removeAllFilter();

  /**
   * @return 返回结果的替换规则
   */
  List<ResponseTransformer> responseTransformer();

  /**
   * 增加结果的替换规则
   *
   * @param transformer
   */
  void addResponseTransformer(ResponseTransformer transformer);

  /**
   * 删除结果的替换规则
   *
   * @param name transformer的名称
   */
  void removeResponseTransformer(String name);

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

  static ApiDefinition create(ApiDefinitionOption option) {
    return new ApiDefinitionImpl(option);
  }

  static ApiDefinition fromJson(JsonObject jsonObject) {
    return null;
//    return ApiDefinitionDecoder.instance().apply(jsonObject);
  }

  /**
   * 根据插件名称返回插件
   *
   * @param name 插件名称
   * @return 如果未找到对应的插件，返回null;
   */
  default ApiPlugin plugin(String name) {
    Preconditions.checkNotNull(name, "name cannot be null");
    List<ApiPlugin> apiPlugins = plugins().stream().filter(p -> p.name().equalsIgnoreCase(name))
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

  default ApiDefinition copy() {
    JsonObject jsonObject = toJson();
    return ApiDefinition.fromJson(jsonObject);
  }

}
