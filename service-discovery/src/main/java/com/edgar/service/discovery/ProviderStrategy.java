package com.edgar.service.discovery;

import io.vertx.servicediscovery.Record;

import java.util.List;

/**
 * 从一组节点中选择一个节点的策略.
 * <p>
 * 主要是用来实现服务发现的负载均衡
 *
 * @author Edgar
 */
public interface ProviderStrategy {
  /**
   * 从给定一组对象中，返回一个对象.
   *
   * @param instances the instance list
   * @return the instance to use
   */
  Record get(List<Record> instances);

  static ProviderStrategy random() {
    return new RandomStrategy();
  }

  static ProviderStrategy weightRoundRobin() {
    return new WeightRoundbinStrategy();
  }

  static ProviderStrategy roundRobin() {
    return new RoundRobinStrategy();
  }

  static ProviderStrategy sticky(ProviderStrategy masterStrategy) {
    return new StickyStrategy(masterStrategy);
  }
}