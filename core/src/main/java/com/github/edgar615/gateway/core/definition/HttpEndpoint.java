package com.github.edgar615.gateway.core.definition;

import io.vertx.core.http.HttpMethod;

/**
 * Created by Edgar on 2017/8/25.
 *
 * @author Edgar  Date 2017/8/25
 */
public interface HttpEndpoint extends Endpoint {
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
}
