package com.github.edgar615.direwolves.http.loadbalance;

import io.vertx.servicediscovery.Record;

import java.util.List;

/**
 * 从一组节点中选择一个节点的策略.
 * <p>
 * 主要是用来实现服务发现的负载均衡
 *
 * @author Edgar
 */
public interface ChooseStrategy {
  /**
   * 从给定一组对象中，返回一个对象.
   *
   * @param instances the instance list
   * @return the instance to use
   */
  Record get(List<Record> instances);

  static ChooseStrategy random() {
    return new RandomStrategy();
  }

  static ChooseStrategy lastConnection() {
    return new LastConnectionStrategy();
  }

  static ChooseStrategy weightRoundRobin() {
    return new WeightRoundbinStrategy();
  }

  static ChooseStrategy roundRobin() {
    return new RoundRobinStrategy();
  }

  static ChooseStrategy sticky(ChooseStrategy masterStrategy) {
    return new StickyStrategy(masterStrategy);
  }
}