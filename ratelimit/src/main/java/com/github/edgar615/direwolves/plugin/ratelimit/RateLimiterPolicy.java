package com.github.edgar615.direwolves.plugin.ratelimit;

import java.util.concurrent.TimeUnit;

/**
 * 限流策略.
 * <p>
 * 所有的接口都共享这些限流策略.
 *
 * @author Edgar  Date 2016/9/8
 */
public interface RateLimiterPolicy {

  /**
   * 限流的名称，需要保持唯一性.
   *
   * @return
   */
  String name();

  /**
   * 限流的key.
   * 可以是常量：device.write，device.read，要可以是变量：$user.userId：基于用户ID $request.client_ip，基于用户IP
   *
   * @return 限制条件
   */
  String key();

  /**
   * 限制数量，它与interval和unit共同组成限流的速率
   *
   * @return
   */
  long limit();

  /**
   * 限流窗口
   *
   * @return
   */
  long interval();

  /**
   * 限流窗口的单位.
   *
   * @return
   */
  TimeUnit unit();

  static RateLimiterPolicy create(String name, String key, long limit, long interval, TimeUnit unit) {
    return new RateLimiterPolicyImpl(name, key, limit, interval, unit);
  }

}
