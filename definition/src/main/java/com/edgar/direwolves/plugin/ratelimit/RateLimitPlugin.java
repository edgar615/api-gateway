package com.edgar.direwolves.plugin.ratelimit;

import com.edgar.direwolves.plugin.ApiPlugin;

import java.util.List;

/**
 * Created by Edgar on 2016/9/26.
 *
 * @author Edgar  Date 2016/9/26
 */
public interface RateLimitPlugin extends ApiPlugin {

  /**
   * 获取API限流的映射关系的列表.
   *
   * @return RateLimitDefinition的不可变集合.
   */
  List<RateLimit> rateLimits();

  /**
   * 向注册表中添加一个限流策略.
   * 映射表中limitBy和type的组合必须唯一.重复添加的数据会覆盖掉原来的策略.
   *
   * @param rateLimit 限流策略.
   */
  void addRateLimit(RateLimit rateLimit);

  /**
   * 根据组合条件查询映射.
   *
   * @param limitBy 限流分类
   * @param type    限流类型
   */
  void removeRateLimit(String limitBy, String type);

  default String name() {
    return "RATE_LIMIT";
  }
}
