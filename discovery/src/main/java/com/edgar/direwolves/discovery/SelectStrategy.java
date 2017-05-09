package com.edgar.direwolves.discovery;

import io.vertx.servicediscovery.Record;

import java.util.List;

/**
 * 从一组节点中选择一个节点的策略.
 * <p>
 * 主要是用来实现服务发现的负载均衡
 *
 * @author Edgar
 */
public interface SelectStrategy {
  /**
   * 从给定一组对象中，返回一个对象.
   *
   * @param instances the instance list
   * @return the instance to use
   */
  Record get(List<Record> instances);

  static SelectStrategy random() {
    return new RandomStrategy();
  }

  static SelectStrategy weightRoundRobin() {
    return new WeightRoundbinStrategy();
  }

  static SelectStrategy roundRobin() {
    return new RoundRobinStrategy();
  }

  static SelectStrategy sticky(SelectStrategy masterStrategy) {
    return new StickyStrategy(masterStrategy);
  }
}