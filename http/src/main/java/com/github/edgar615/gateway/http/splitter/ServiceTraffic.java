package com.github.edgar615.gateway.http.splitter;

import com.github.edgar615.gateway.core.dispatch.ApiContext;

/**
 * API版本的切流判断接口.
 *
 * @author Edgar  Date 2018/2/10
 */
public interface ServiceTraffic {

    /**
     * 根据上下文，返回合适的服务，如果没有匹配到切流策略，返回null
     *
     * @param apiContext
     * @return
     */
    String decision(ApiContext apiContext);
}
