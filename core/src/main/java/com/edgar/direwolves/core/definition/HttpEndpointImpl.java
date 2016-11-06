package com.edgar.direwolves.core.definition;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import io.vertx.core.http.HttpMethod;

/**
 * HTTP的远程调用定义.
 *
 * @author Edgar  Date 2016/9/12
 */
class HttpEndpointImpl implements HttpEndpoint {

  /**
   * 服务名
   */
  private final String name;

  /**
   * 请求方法 GET | POST | DELETE | PUT
   */
  private final HttpMethod method;

  /**
   * 远程rest路径
   * 示例：/tasks
   * 示例：/tasks/$1/abandon，$1表示当前请求上下文中的$1变量
   */
  private final String path;

//    /**
//     * 描述
//     */
//    private final String description;

  /**
   * 远程服务名，可以通过服务发现机制查找到对应的服务地址
   */
  private final String service;

  HttpEndpointImpl(String name, HttpMethod method, String path, String service) {
    Preconditions.checkNotNull(name, "name can not be null");
    Preconditions.checkNotNull(method, "method can not be null");
    Preconditions.checkNotNull(path, "path can not be null");
    Preconditions.checkNotNull(service, "service can not be null");
    if (!path.startsWith("/")) {
      path = "/" + path;
    }
    if (path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }

    this.name = name;
    this.method = method;
    this.path = path;
    this.service = service;
  }

  @Override
  public String name() {
    return this.name;
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
  public String service() {
    return service;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper("HttpEndpoint")
        .add("name", name)
        .add("service", service)
        .add("path", path)
        .add("method", method.name())
        .toString();
  }


}
