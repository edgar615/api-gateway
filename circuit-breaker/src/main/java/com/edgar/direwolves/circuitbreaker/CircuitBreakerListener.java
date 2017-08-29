package com.edgar.direwolves.circuitbreaker;

/**
 * 断路器的监听器.
 *
 * 因为CircuitBreakerRegistry的LoadCache加载断路器的时候无法指定具体的监听器，这个对象已经废弃，改为使用eventbus通知
 * @author Edgar  Date 2017/8/25
 */
@Deprecated
public interface CircuitBreakerListener {

  void onCircuitBreaker(CircuitBreakerNotification notification);
}
