package com.edgar.direwolves.definition;

import io.vertx.core.http.HttpMethod;

import java.util.List;

/**
 * Created by Edgar on 2016/9/14.
 *
 * @author Edgar  Date 2016/9/14
 */
public interface HttpEndpoint extends Endpoint {
    String TYPE = "http-endpoint";

    default String type() {
        return TYPE;
    }

    /**
     * @return 名称，必填项，全局唯一.
     */
    String name();

    /**
     * @return 请求方法 GET | POST | DELETE | PUT.
     */
    HttpMethod method();

    /**
     * API路径
     * 示例：/tasks，匹配请求：/tasks.
     * 示例：/tasks/$param1，匹配请求：/tasks/变量param1.
     *
     * @return API路径
     */
    String path();

    /**
     *
     * @return 服务名，用于服务发现.
     */
    String service();

    /**
     * @return URL参数
     */
    List<Parameter> urlArgs();

    /**
     * @return body参数
     */
    List<Parameter> bodyArgs();

    /**
     *
     * @return 响应结果是否是JSON数组，如果是，则按JSON数组处理。
     */
    boolean isArray();


    static HttpEndpointBuilder builder() {
        return new HttpEndpointBuilder();
    }
}
