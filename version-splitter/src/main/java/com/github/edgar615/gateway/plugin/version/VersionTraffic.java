package com.github.edgar615.gateway.plugin.version;

import com.github.edgar615.gateway.core.dispatch.ApiContext;

/**
 * API版本的切流判断接口.
 *
 * @author Edgar  Date 2018/2/10
 */
public interface VersionTraffic {

    /**
     * 根据上下文，返回合适的版本号，如果没有匹配到切流策略，返回null
     *
     * @param apiContext
     * @return
     */
    String decision(ApiContext apiContext);
}
