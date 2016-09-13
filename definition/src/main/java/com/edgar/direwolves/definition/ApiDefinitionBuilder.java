package com.edgar.direwolves.definition;

import io.vertx.core.http.HttpMethod;

import java.util.List;

class ApiDefinitionBuilder {

    private static final String SCOPE = "default";

    /**
     * 服务名
     */
    private String name;

    /**
     * 请求方法 GET | POST | DELETE | PUT
     */
    private HttpMethod method = HttpMethod.GET;

    /**
     * 远程rest路径
     * 示例：/tasks
     * 示例：/tasks/$1/abandon，$1表示当前请求上下文中的$1变量
     */
    private String path;

    /**
     * 权限范围，默认default;
     */
    private String scope = SCOPE;

    /**
     * URL参数
     */
    private List<Parameter> urlArgs;

    /**
     * body参数
     */
    private List<Parameter> bodyArgs;

    /**
     * 远程请求定义.
     */
    private List<Endpoint> endpoints;


    ApiDefinitionBuilder() {
    }

    public ApiDefinitionBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public ApiDefinitionBuilder setMethod(HttpMethod method) {
        this.method = method;
        return this;
    }

    public ApiDefinitionBuilder setPath(String path) {
        this.path = path;
        return this;
    }

    public ApiDefinitionBuilder setScope(String scope) {
        this.scope = scope;
        return this;
    }

    public ApiDefinitionBuilder setUrlArgs(List<Parameter> urlArgs) {
        this.urlArgs = urlArgs;
        return this;
    }

    public ApiDefinitionBuilder setEndpoints(List<Endpoint> endpoints) {
        this.endpoints = endpoints;
        return this;
    }

    public ApiDefinitionBuilder setBodyArgs(List<Parameter> bodyArgs) {
        this.bodyArgs = bodyArgs;
        return this;
    }

    public ApiDefinition build() {
        return new ApiDefinitionImpl(name, method, path, scope, urlArgs, bodyArgs, endpoints);
    }
}