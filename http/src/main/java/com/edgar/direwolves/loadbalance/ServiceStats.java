package com.edgar.direwolves.loadbalance;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 描述服务节点的状态.
 *
 * 目前定义了这些状态：
 *  circuitBreakerTripped：断路器打开
 *
 * Created by edgar on 17-7-29.
 */
public class ServiceStats {

  /**
   * 服务节点ID
   */
  private final String serviceId;

  /**
   * 断路器是否打开
   */
  private boolean circuitBreakerTripped;

  /**
   * 当前请求数，请求开始的+1，请求
   */
  private AtomicInteger activeRequests = new AtomicInteger();

  /**
   * 服务节点的权重，需要基于服务的响应时间动态变化
   */
  private AtomicInteger weight = new AtomicInteger(60);

  /**
   * 服务节点的选择权重，主要用在权重选择策略.
   */
  private AtomicInteger effectiveWeight = new AtomicInteger(60);

  public ServiceStats(String serviceId) {this.serviceId = serviceId;}

//  private AtomicLong activeConnectionsLimit

  public boolean isCircuitBreakerTripped() {
    return circuitBreakerTripped;
  }

  public ServiceStats setCircuitBreakerTripped(boolean circuitBreakerTripped) {
    this.circuitBreakerTripped = circuitBreakerTripped;
    return this;
  }

  public ServiceStats incActiveRequests() {
    activeRequests.incrementAndGet();
    return this;
  }

  public ServiceStats decActiveRequests() {
    activeRequests.decrementAndGet();
    return this;
  }

  public ServiceStats incWeight(int num) {
    weight.accumulateAndGet(num, (left, right) -> Math.min(left + right, 100));
    return this;
  }

  public ServiceStats decWeight(int num) {
    weight.accumulateAndGet(num, (left, right) -> Math.max(left - right, 0));
    return this;
  }

  public ServiceStats incEffectiveWeight(int num) {
    effectiveWeight.accumulateAndGet(num, (left, right) -> left + right);
    return this;
  }

  public ServiceStats decEffectiveWeight(int num) {
    effectiveWeight.accumulateAndGet(num, (left, right) -> left - right);
    return this;
  }

  public int activeRequests() {
    return activeRequests.get();
  }

  public int weight() {
    return weight.get();
  }

  public int effectiveWeight() {
    return effectiveWeight.get();
  }

  public String serviceId() {
    return serviceId;
  }

  public void setWeight(int weight) {
    this.weight.set(weight);
  }
}
