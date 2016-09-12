package com.edgar.direwolves.definition;

import com.edgar.util.base.MorePreconditions;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import io.vertx.core.http.HttpMethod;

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
 * <li>url_arg 查询参数，参考查询参数的定义</li>
 * <li>body_arg body参数，参考body参数的定义</li>
 * <li>description 描述</li>
 * <li>failture_policy 远程调用遇到错误之后对处理策略，默认值fail：直接返回错误信息，如果有多个错误信息，会按照endpont的定义顺序取出第一条信息，origin：与远程调用对返回值保持一致，custom：自定义对错误信息</li>
 * <li>custom_error:如果failture_policy=custom，该值为必填项，必须满足{code:xxx,message:xxx}的格式</li>
 * <li>endpoints 远程服务对定义，JSON数组，参考Endpoint的定义</li>
 * </ul>
 *
 * @author Edgar  Date 2016/9/8
 */
public class ApiDefinition {

    private static final String SCOPE = "default";

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
     * 权限范围，默认default;
     */
    private final String scope;

    /**
     * URL参数
     */
    private final List<Parameter> urlArgs;

    /**
     * body参数
     */
    private final List<Parameter> bodyArgs;

    /**
     * 远程请求定义.
     */
    private final List<Endpoint> endpoints;

    private ApiDefinition(String name, HttpMethod method, String path, String scope, List<Parameter> urlArgs, List<Parameter> bodyArgs, List<Endpoint> endpoints) {
        Preconditions.checkNotNull(name, "name can not be null");
        Preconditions.checkNotNull(method, "method can not be null");
        Preconditions.checkNotNull(path, "path can not be null");
        Preconditions.checkNotNull(scope, "service can not be null");
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
        this.scope = scope;
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
        this.endpoints = ImmutableList.copyOf(endpoints);
        this.pattern = Pattern.compile(path);
    }

    static Builder builder() {
        return new Builder();
    }

    public String getName() {
        return name;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public String getScope() {
        return scope;
    }

    public List<Parameter> getUrlArgs() {
        return urlArgs;
    }

    public List<Parameter> getBodyArgs() {
        return bodyArgs;
    }

    public List<Endpoint> getEndpoints() {
        return endpoints;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("ApiDefinition")
                .add("name", name)
                .add("method", method)
                .add("path", path)
                .add("urlArgs", urlArgs)
                .add("bodyArgs", bodyArgs)
                .add("scope", scope)
                .add("endpoints", endpoints)
                .toString();
    }

    static class Builder {
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


        private Builder() {
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setMethod(HttpMethod method) {
            this.method = method;
            return this;
        }

        public Builder setPath(String path) {
            this.path = path;
            return this;
        }

        public Builder setScope(String scope) {
            this.scope = scope;
            return this;
        }

        public Builder setUrlArgs(List<Parameter> urlArgs) {
            this.urlArgs = urlArgs;
            return this;
        }

        public Builder setEndpoints(List<Endpoint> endpoints) {
            this.endpoints = endpoints;
            return this;
        }

        public Builder setBodyArgs(List<Parameter> bodyArgs) {
            this.bodyArgs = bodyArgs;
            return this;
        }

        public ApiDefinition build() {
            return new ApiDefinition(name, method, path, scope, urlArgs, bodyArgs, endpoints);
        }
    }
}
