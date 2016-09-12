package com.edgar.direwolves.definition;

import io.vertx.core.http.HttpMethod;

import java.util.List;

/**
 * 定义远程服务调用的格式.
 *
 * @author Edgar  Date 2016/9/12
 */
public interface Endpoint {

    /**
     * @return endpoint的名称
     */
    String name();

    /**
     * @return endpoint的类型
     */
    String type();

//    static HttpEndpoint createHttp(String name, HttpMethod method, String path, String service, List<Parameter> urlArgs, List<Parameter> bodyArgs, boolean isArray) {
//        return new HttpEndpoint(name, method, path, service, urlArgs, bodyArgs, isArray);
//    }
//
//    static HttpEndpoint createHttp(String name, HttpMethod method, String path, String service, List<Parameter> urlArgs) {
//        return new HttpEndpoint(name, method, path, service, urlArgs, null, false);
//    }
//
//    static HttpEndpoint createHttp(String name, HttpMethod method, String path, String service, List<Parameter> urlArgs, boolean isArray) {
//        return new HttpEndpoint(name, method, path, service, urlArgs, null, isArray);
//    }
}
