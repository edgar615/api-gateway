package com.edgar.direwolves.plugin.ratelimit;

import com.google.common.collect.ImmutableList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by Edgar on 2016/10/21.
 *
 * @author Edgar  Date 2016/10/21
 */
public class RateLimitPluginImpl implements RateLimitPlugin {

  private final Set<RateLimit> rateLimits = new HashSet<>();

  @Override
  public List<RateLimit> rateLimits() {
    return ImmutableList.copyOf(rateLimits);
  }

  @Override
  public void addRateLimit(RateLimit definition) {
    List<RateLimit> filterDefintions = rateLimits.stream()
            .filter(d -> definition.limitBy().equalsIgnoreCase(d.limitBy())
                         && definition.type().equalsIgnoreCase(d.type()))
            .collect(Collectors.toList());
    rateLimits.add(definition);
    rateLimits.removeAll(filterDefintions);
  }

  /**
   * 根据组合条件查询映射.
   *
   * @param limitBy 限流分类
   * @param type    限流类型
   */
  @Override
  public void removeRateLimit(String limitBy, String type) {
    Predicate<RateLimit> predicate = rateLimit -> true;
    if (limitBy != null) {
      predicate = predicate.and(rateLimit -> limitBy.equalsIgnoreCase(rateLimit.limitBy()));
    }
    if (type != null) {
      predicate = predicate.and(rateLimit -> type.equalsIgnoreCase(rateLimit.type()));
    }
    this.rateLimits.removeIf(predicate);
  }
}
