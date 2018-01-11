package com.github.edgar615.direwolves.core.definition;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import com.github.edgar615.util.base.MorePreconditions;
import io.vertx.core.http.HttpMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * API的路由转发定义.
 * 该类仅定义参数校验，转发规则.对于其他逻辑交由其他的定义类，如<code>AuthDefinition</code>
 * <ul>
 * <li>name 名称，必填项</li>
 * <li>path 路径，可使用正则表达式，必填项</li>
 * <li>method 方法 GET POST PUT DELETE，必填项</li>
 * <li>scope 表示权限范围，默认为default</li>
 * <li>url.arg 查询参数，参考查询参数的定义</li>
 * <li>body.arg body参数，参考body参数的定义</li>
 * <li>description 描述</li>
 * <li>failture_policy 远程调用遇到错误之后对处理策略，默认值fail：直接返回错误信息，如果有多个错误信息，会按照endpont的定义顺序取出第一条信息，origin
 * ：与远程调用对返回值保持一致，custom：自定义对错误信息</li>
 * <li>custom_error:如果failture_policy=custom，该值为必填项，必须满足{code:xxx,message:xxx}的格式</li>
 * <li>endpoints 远程服务对定义，JSON数组，参考Endpoint的定义</li>
 * </ul>
 *
 * @author Edgar  Date 2016/9/8
 */
class ApiDefinitionImpl implements ApiDefinition {

  /**
   * 名称，必填项，全局唯一
   */
  private final String name;

  /**
   * 请求方法 GET | POST | DELETE | PUT.
   */
  private final HttpMethod method;

  /**
   * API路径
   * 示例：/tasks，匹配请求：/tasks.
   * 示例：/tasks，匹配请求：/tasks.
   * 示例：/tasks/([\\d+]+)/abandon，匹配请求/tasks/123/abandon
   */
  private final String path;


  /**
   * 路径的正则表达式.在目前的设计中，它和path保持一致.
   */
  private final Pattern pattern;

//    /**
//     * 描述
//     */
//    private String description;

  /**
   * 远程请求定义.
   */
  private final List<Endpoint> endpoints;

  private final List<ApiPlugin> plugins = new ArrayList<>();

  ApiDefinitionImpl(String name, HttpMethod method, String path,
                    List<Endpoint> endpoints,
                    Pattern pattern) {
    Preconditions.checkNotNull(name, "name can not be null");
    Preconditions.checkNotNull(method, "method can not be null");
    Preconditions.checkNotNull(path, "path can not be null");
    Preconditions.checkNotNull(endpoints, "endpoints can not be null");
    MorePreconditions.checkNotEmpty(endpoints, "endpoints can not be empty");
    if (!path.startsWith("/")) {
      path = "/" + path;
    }
    if (path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }

    this.name = name;
    this.method = method;
    this.path = path;
    this.endpoints = ImmutableList.copyOf(endpoints);
    this.pattern = pattern;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public HttpMethod method() {
    return method;
  }

  @Override
  public String path() {
    return path;
  }

  @Override
  public Pattern pattern() {
    return pattern;
  }

  @Override
  public List<Endpoint> endpoints() {
    return endpoints;
  }

  @Override
  public List<ApiPlugin> plugins() {
    return ImmutableList.copyOf(plugins);
  }

  @Override
  public ApiDefinition addPlugin(ApiPlugin plugin) {
    if (plugin == null) {
      return this;
    }
    Preconditions.checkNotNull(plugin, "plugin cannot be null");
    removePlugin(plugin.name());
    plugins.add(plugin);
    return this;
  }

  @Override
  public ApiDefinition removePlugin(String name) {
    Preconditions.checkNotNull(name, "name cannot be null");
    ApiPlugin apiPlugin = plugin(name);
    if (apiPlugin != null) {
      plugins.remove(apiPlugin);
    }
    return this;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper("ApiDefinition")
            .add("name", name)
            .add("method", method)
            .add("path", path)
            .add("endpoints", endpoints)
            .add("plugins", plugins)
            .toString();
  }

}
