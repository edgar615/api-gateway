package com.github.edgar615.gateway.plugin.ratelimit;

import com.github.edgar615.gateway.core.definition.ApiPlugin;

import java.util.List;

/**
 * Created by Edgar on 2016/9/26.
 *
 * @author Edgar  Date 2016/9/26
 */
public interface RateLimiterPlugin extends ApiPlugin {

  /**
   * 获取API限流的映射关系的列表.
   *
   * @return RateLimiter的不可变集合.
   */
  List<RateLimiter> rateLimiters();

  /**
   * 向注册表中添加一个限流策略.
   * 限流的名称必须唯一.重复添加的数据会覆盖掉原来的策略.
   *
   * @param rateLimiter 限流策略.
   */
  void addRateLimiter(RateLimiter rateLimiter);

  /**
   * 根据名称删除限流策略.
   *
   * @param name 限流名称
   */
  void removeRateLimiter(String name);

  static RateLimiterPlugin create() {
    return new RateLimiterPluginImpl();
  }

  @Override
  default String name() {
    return RateLimiterPlugin.class.getSimpleName();
  }
}
