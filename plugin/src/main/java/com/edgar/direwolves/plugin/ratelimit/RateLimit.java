package com.edgar.direwolves.plugin.ratelimit;

/**
 * 限流策略.
 * second
 * minute
 * hour
 * day
 * month
 * year
 * limit_by 限制条件：ip、token、app_key
 * policy 缓存策略，默认local，还支持cluster，redis，如果开启redis需要检查redis是否可以连接
 *
 * @author Edgar  Date 2016/9/8
 */
public interface RateLimit {

  static RateLimit create(String limitBy, String type, long limit) {
    return new RateLimitImpl(limitBy, type, limit);
  }

  /**
   * 限制条件,user_rate | app_key_rate
   *
   * @return 限制条件
   */
  String limitBy();

  /**
   * 限制类型  second | minute | hour | day | month | year
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

}
