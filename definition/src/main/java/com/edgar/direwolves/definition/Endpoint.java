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
     * 创建HTTP类型的Endpoint
     * @param name 名称
     * @param method 请求方法 GET | POST | DELETE | PUT
     * @param path API路径
     * @param service 服务名，用于服务发现
     * @param urlArgs URL参数
     * @param bodyArgs body参数
     * @return HttpEndpoint
     */
    static HttpEndpoint createHttp(String name, HttpMethod method, String path, String service, List<Parameter> urlArgs, List<Parameter> bodyArgs) {
        return new HttpEndpointImpl(name, method, path, service, urlArgs, bodyArgs);
    }

    /**
     * 创建HTTP类型的Endpoint
     * @param name 名称
     * @param method 请求方法 GET | POST | DELETE | PUT
     * @param path API路径
     * @param service 服务名，用于服务发现
     * @param urlArgs URL参数
     * @return HttpEndpoint
     */
    static HttpEndpoint createHttp(String name, HttpMethod method, String path, String service, List<Parameter> urlArgs) {
        return new HttpEndpointImpl(name, method, path, service, urlArgs, null);
    }

    /**
     * @return endpoint的名称
     */
    String name();

    /**
     * @return endpoint的类型
     */
    String type();
}
