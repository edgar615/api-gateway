package com.github.edgar615.direwolves.core.definition;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import com.github.edgar615.util.base.MorePreconditions;
import io.vertx.core.http.HttpMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * API的路由转发定义.
 * 该类仅定义参数校验，转发规则.对于其他逻辑交由插件实现.
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
   * 示例：/tasks/123，匹配请求：/tasks/123.
   */
  private final String path;

  /**
   * 远程请求定义.
   */
  private final List<Endpoint> endpoints;

  private final List<ApiPlugin> plugins = new ArrayList<>();

  ApiDefinitionImpl(String name, HttpMethod method, String path,
                    List<Endpoint> endpoints) {
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
