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
public class HttpEndpoint implements Endpoint {
    private static final String TYPE = "http-endpoint";

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

    /**
     * rest接口的响应是否是JSON数组，true表示是，false表示不是.
     */
    private final boolean isArray;

    private HttpEndpoint(String name, HttpMethod method, String path, String service, List<Parameter> urlArgs, List<Parameter> bodyArgs, boolean isArray) {
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
        this.isArray = isArray;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String type() {
        return TYPE;
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

    public String getService() {
        return service;
    }

    public List<Parameter> getUrlArgs() {
        return urlArgs;
    }

    public List<Parameter> getBodyArgs() {
        return bodyArgs;
    }

    public boolean isArray() {
        return isArray;
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
                .add("isArray;", isArray)
                .toString();
    }

    public static class Builder {
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
         * 远程服务名，可以通过服务发现机制查找到对应的服务地址
         */
        private String service;


        /**
         * URL参数
         */
        private List<Parameter> urlArgs;

        /**
         * body参数
         */
        private List<Parameter> bodyArgs;

        /**
         * rest接口的响应是否是JSON数组，true表示是，false表示不是.
         */
        private boolean isArray = false;

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

        public Builder setService(String service) {
            this.service = service;
            return this;
        }

        public Builder setUrlArgs(List<Parameter> urlArgs) {
            this.urlArgs = urlArgs;
            return this;
        }

        public Builder setBodyArgs(List<Parameter> bodyArgs) {
            this.bodyArgs = bodyArgs;
            return this;
        }

        public Builder setArray(boolean isArray) {
            this.isArray = isArray;
            return this;
        }

        public HttpEndpoint build() {
            return new HttpEndpoint(name, method, path, service, urlArgs, bodyArgs, isArray);
        }
    }
}
