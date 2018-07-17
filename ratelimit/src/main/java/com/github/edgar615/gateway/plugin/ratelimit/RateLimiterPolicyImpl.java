package com.github.edgar615.gateway.plugin.ratelimit;

import com.google.common.base.MoreObjects;

import java.util.concurrent.TimeUnit;

/**
 * 限流策略.
 *
 * @author Edgar  Date 2016/9/8
 */
class RateLimiterPolicyImpl implements RateLimiterPolicy {

  /**
   * 限流名称
   */
  private final String name;

  /**
   * 限制条件
   */
  private final String key;

  /**
   * 最大请求数量
   */
  private final long limit;

  /**
   * 限流窗口
   */
  private final long interval;

  /**
   * 限流窗口的单位.
   */
  private final TimeUnit unit;

  RateLimiterPolicyImpl(String name, String key, long limit, long interval, TimeUnit unit) {
    this.name = name;
    this.key = key;
    this.limit = limit;
    this.interval = interval;
    this.unit = unit;
  }


  @Override
  public String name() {
    return name;
  }

  @Override
  public String key() {
    return key;
  }

  @Override
  public long limit() {
    return limit;
  }

  @Override
  public long interval() {
    return interval;
  }

  @Override
  public TimeUnit unit() {
    return unit;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper("RateLimiterPolicy")
            .add("name", name)
            .add("key", key)
            .add("limit", limit)
            .add("interval", interval)
            .add("unit", unit)
            .toString();
  }

}
