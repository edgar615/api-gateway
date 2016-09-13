package com.edgar.direwolves.definition;

import io.vertx.core.http.HttpMethod;

import java.util.List;
import java.util.regex.Pattern;

/**
 * API定义的接口.
 *
 * @author Edgar  Date 2016/9/13
 */
public interface ApiDefinition {

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
     * 示例：/tasks，匹配请求：/tasks.
     * 示例：/tasks/([\\d+]+)/abandon，匹配请求/tasks/123/abandon
     *
     * @return API路径
     */
    String path();

    /**
     * @return 路径的正则表达式.在目前的设计中，它和path保持一致.
     */
    Pattern pattern();

    /**
     * @return 权限范围
     */
    String scope();

    /**
     * @return URL参数
     */
    List<Parameter> urlArgs();

    /**
     * @return body参数
     */
    List<Parameter> bodyArgs();

    /**
     * @return 远程请求定义
     */
    List<Endpoint> endpoints();
}
