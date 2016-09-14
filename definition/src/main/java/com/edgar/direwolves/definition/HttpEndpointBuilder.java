package com.edgar.direwolves.definition;

import io.vertx.core.http.HttpMethod;

import java.util.List;

public class HttpEndpointBuilder {
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

        HttpEndpointBuilder() {
        }

        public HttpEndpointBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public HttpEndpointBuilder setMethod(HttpMethod method) {
            this.method = method;
            return this;
        }

        public HttpEndpointBuilder setPath(String path) {
            this.path = path;
            return this;
        }

        public HttpEndpointBuilder setService(String service) {
            this.service = service;
            return this;
        }

        public HttpEndpointBuilder setUrlArgs(List<Parameter> urlArgs) {
            this.urlArgs = urlArgs;
            return this;
        }

        public HttpEndpointBuilder setBodyArgs(List<Parameter> bodyArgs) {
            this.bodyArgs = bodyArgs;
            return this;
        }

        public HttpEndpointBuilder setArray(boolean isArray) {
            this.isArray = isArray;
            return this;
        }

        public HttpEndpoint build() {
            return new HttpEndpointImpl(name, method, path, service, urlArgs, bodyArgs, isArray);
        }
    }