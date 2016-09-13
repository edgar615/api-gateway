package com.edgar.direwolves.definition;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * 限流策略.
 * second
 * minute
 * hour
 * day
 * month
 * year
 * limit_by 限制条件：ip、token、appkey
 * policy 缓存策略，默认local，还支持cluster，redis，如果开启redis需要检查redis是否可以连接
 *
 * @author Edgar  Date 2016/9/8
 */
public class RateLimitDefinition {

    /**
     * api名称
     */
    private final String apiName;
    /**
     * 限制条件
     */
    private final RateLimitBy rateLimitBy;

    /**
     * 限制类型
     */
    private final RateLimitType rateLimitType;

    /**
     * 最大请求数量
     */
    private final long limit;

    public RateLimitDefinition(String apiName, RateLimitBy rateLimitBy, RateLimitType rateLimitType, long limit) {
        Preconditions.checkNotNull(apiName, "apiName can not be null");
        Preconditions.checkNotNull(rateLimitBy, "rateLimitBy can not be null");
        Preconditions.checkNotNull(rateLimitType, "rateLimitType can not be null");
        Preconditions.checkArgument(limit > 0, "limit must > 0");
        this.apiName = apiName;
        this.rateLimitBy = rateLimitBy;
        this.rateLimitType = rateLimitType;
        this.limit = limit;
    }

    public RateLimitBy getRateLimitBy() {
        return rateLimitBy;
    }

    public RateLimitType getRateLimitType() {
        return rateLimitType;
    }

    public long getLimit() {
        return limit;
    }

    public String getApiName() {
        return apiName;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("RateLimitDefinition")
                .add("apiName", apiName)
                .add("rateLimitBy", rateLimitBy)
                .add("rateLimitType", rateLimitType)
                .add("limit", limit)
                .toString();
    }
}
