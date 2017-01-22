package com.edgar.direwolves.plugin.ratelimit;

/**
 * 限流策略.
 * second
 * minute
 * hour
 * day
 * limit_by 限制条件：ip、app_key
 *
 * @author Edgar  Date 2016/9/8
 */
public interface RateLimit {

  /**
   * 限制条件,user | appkey | ip
   *
   * @return 限制条件
   */
  String key();

  /**
   * 限制类型  second | minute | hour | day
   *
   * @return 限制类型
   */
  String type();

  /**
   * 限制数量
   *
   * @return
   */
  long limit();

  static RateLimit create(String key, String type, long limit) {
    return new RateLimitImpl(key, type, limit);
  }

}
