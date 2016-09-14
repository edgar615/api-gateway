package com.edgar.direwolves.definition;

/**
 * 限流策略.
 * second
 * minute
 * hour
 * day
 * month
 * year
 * limit_by 限制条件：ip、token、app_key
 * policy 缓存策略，默认local，还支持cluster，redis，如果开启redis需要检查redis是否可以连接
 *
 * @author Edgar  Date 2016/9/8
 */
public interface RateLimitDefinition {

    static RateLimitDefinition create(String apiName, RateLimitBy rateLimitBy, RateLimitType rateLimitType, long limit) {
        return new RateLimitDefinitionImpl(apiName, rateLimitBy, rateLimitType, limit);
    }

    /**
     * 限制条件,user | token | app_key
     *
     * @return 限制条件
     */
    RateLimitBy rateLimitBy();

    /**
     * 限制类型  second | minute | hour | day | month | year
     *
     * @return 限制类型
     */
    RateLimitType rateLimitType();

    /**
     * 限制数量
     *
     * @return
     */
    long limit();

    /**
     * api名称
     *
     * @return api名称
     */
    String apiName();
}
