package com.edgar.direwolves.definition;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import java.util.Set;

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
public class RateLimitDefinitionImpl implements RateLimitDefinition {
    /**
     * 限制条件
     */
    private final String limitBy;

    /**
     * 限制类型
     */
    private final String type;

    /**
     * 最大请求数量
     */
    private final long limit;

    private final Set<String> optionalTypes = ImmutableSet.of("second", "minute", "hour", "day", "month", "year");
    private final Set<String> optionalLimits = ImmutableSet.of("ip", "token", "app_key");

    RateLimitDefinitionImpl(String limitBy, String type, long limit) {
        Preconditions.checkArgument(optionalLimits.contains(limitBy), "limitBy must be ip | token | app_key");
        Preconditions.checkArgument(optionalTypes.contains(type), "type must be second | minute | hour | day | month | year");
        Preconditions.checkArgument(limit > 0, "limit must > 0");
        this.limitBy = limitBy;
        this.type = type;
        this.limit = limit;
    }


    @Override
    public String limitBy() {
        return limitBy;
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public long limit() {
        return limit;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("RateLimitDefinition")
                .add("limitBy", limitBy)
                .add("type", type)
                .add("limit", limit)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RateLimitDefinitionImpl that = (RateLimitDefinitionImpl) o;

        if (limit != that.limit) return false;
        if (limitBy != null ? !limitBy.equals(that.limitBy) : that.limitBy != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = limitBy != null ? limitBy.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (int) (limit ^ (limit >>> 32));
        return result;
    }
}
