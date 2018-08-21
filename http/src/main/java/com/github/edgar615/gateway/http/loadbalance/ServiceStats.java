package com.github.edgar615.gateway.http.loadbalance;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 描述服务节点的状态.
 * <p>
 * 目前定义了这些状态：
 * circuitBreakerTripped：断路器打开
 * <p>
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

    /**
     * 服务的断路器是否已经打开
     *
     * @return
     */
    public boolean isCircuitBreakerTripped() {
        return circuitBreakerTripped;
    }

    /**
     * 设置断路器状态
     *
     * @param circuitBreakerTripped
     * @return
     */
    public ServiceStats setCircuitBreakerTripped(boolean circuitBreakerTripped) {
        this.circuitBreakerTripped = circuitBreakerTripped;
        return this;
    }

    /**
     * 增加当前请求
     *
     * @return
     */
    public ServiceStats incActiveRequests() {
        activeRequests.incrementAndGet();
        return this;
    }

    /**
     * 减少当前请求
     *
     * @return
     */
    public ServiceStats decActiveRequests() {
        activeRequests.decrementAndGet();
        return this;
    }

    /**
     * 增加权重，根据响应判断
     *
     * @param num
     * @return
     */
    public ServiceStats incWeight(int num) {
        weight.accumulateAndGet(num, (left, right) -> Math.min(left + right, 100));
        return this;
    }

    /**
     * 降低权重，根据响应判断
     *
     * @param num
     * @return
     */
    public ServiceStats decWeight(int num) {
        weight.accumulateAndGet(num, (left, right) -> Math.max(left - right, 0));
        return this;
    }

    /**
     * 增加有效权重，根据权重选择判断
     *
     * @param num
     * @return
     */
    public ServiceStats incEffectiveWeight(int num) {
        effectiveWeight.accumulateAndGet(num, (left, right) -> left + right);
        return this;
    }

    /**
     * 降低有效权重，根据权重选择判断
     *
     * @param num
     * @return
     */
    public ServiceStats decEffectiveWeight(int num) {
        effectiveWeight.accumulateAndGet(num, (left, right) -> left - right);
        return this;
    }

    /**
     * 当前请求数
     *
     * @return
     */
    public int activeRequests() {
        return activeRequests.get();
    }

    /**
     * 当前权重
     *
     * @return
     */
    public int weight() {
        return weight.get();
    }

    /**
     * 当前有效权重
     *
     * @return
     */
    public int effectiveWeight() {
        return effectiveWeight.get();
    }

    public String serviceId() {
        return serviceId;
    }

    /**
     * 重置权重
     *
     * @param weight
     */
    public void setWeight(int weight) {
        this.weight.set(weight);
    }
}
