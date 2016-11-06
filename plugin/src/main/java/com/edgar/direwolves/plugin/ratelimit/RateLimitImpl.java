package com.edgar.direwolves.plugin.ratelimit;

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
 * limit_by 限制条件：ip、token、app_key、user
 *
 * @author Edgar  Date 2016/9/8
 */
class RateLimitImpl implements RateLimit {
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

  private final Set<String> optionalTypes =
      ImmutableSet.of("second", "minute", "hour", "day", "month", "year");

  private final Set<String> optionalLimits = ImmutableSet.of("ip", "token", "app_key", "user");

  RateLimitImpl(String limitBy, String type, long limit) {
    Preconditions.checkArgument(optionalLimits.contains(limitBy),
        "limitBy must be ip | token | app_key");
    Preconditions.checkArgument(optionalTypes.contains(type),
        "type must be second | minute | hour | day | month | year");
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
    return MoreObjects.toStringHelper("RateLimit")
        .add("limitBy", limitBy)
        .add("type", type)
        .add("limit", limit)
        .toString();
  }

}
