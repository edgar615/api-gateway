package com.github.edgar615.gateway.plugin.ratelimit;

import com.google.common.base.MoreObjects;

/**
 * Created by Edgar on 2017/6/22.
 *
 * @author Edgar  Date 2017/6/22
 */
public class RateLimiter {
  private final String name;

  private final long burst;

  public RateLimiter(String name, long burst) {
    this.name = name;
    this.burst = burst;
  }

  public String name() {
    return name;
  }

  public long burst() {
    return burst;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper("RateLimiter")
            .add("name", name)
            .add("burst", burst)
            .toString();
  }
}
