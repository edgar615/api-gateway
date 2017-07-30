package com.edgar.direwolves.loadbalance;

import io.vertx.servicediscovery.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Edgar on 2017/7/28.
 *
 * @author Edgar  Date 2017/7/28
 */
class LoadbalanceImpl implements LoadBalance {

  private ChooseStrategy strategy = ChooseStrategy.roundRobin();

  private final List<ServiceFilter> filters = new ArrayList<>();

  public LoadBalance withStrategy(ChooseStrategy strategy) {
    Objects.requireNonNull(strategy, "ChooseStrategy");
    this.strategy = strategy;
    return this;
  }

  public LoadBalance addFilter(ServiceFilter filter) {
    Objects.requireNonNull(filter, "ServiceFilter");
    filters.add(filter);
    return this;
  }

  public Record choose() {
    return null;
  }
}
