package com.edgar.direwolves.definition;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import io.vertx.core.http.HttpMethod;

import java.util.List;

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


    /**
     * URL参数
     */
    private final List<Parameter> urlArgs;

    /**
     * body参数
     */
    private final List<Parameter> bodyArgs;

    HttpEndpointImpl(String name, HttpMethod method, String path, String service, List<Parameter> urlArgs, List<Parameter> bodyArgs) {
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
        if (urlArgs != null) {
            this.urlArgs = ImmutableList.copyOf(urlArgs);
        } else {
            this.urlArgs = null;
        }
        if (bodyArgs != null) {
            Preconditions.checkArgument(HttpMethod.PUT == method || HttpMethod.POST == method,
                    "can not set body on post|put method");
            this.bodyArgs = ImmutableList.copyOf(bodyArgs);
        } else {
            this.bodyArgs = null;
        }
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
    public List<Parameter> urlArgs() {
        return urlArgs;
    }

    @Override
    public List<Parameter> bodyArgs() {
        return bodyArgs;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("HttpEndpoint")
                .add("name", name)
                .add("service", service)
                .add("path", path)
                .add("method", method)
                .add("urlArgs", urlArgs)
                .add("bodyArgs", bodyArgs)
                .toString();
    }

}
