package com.edgar.direwolves.filter.servicediscovery;

import io.vertx.servicediscovery.Record;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询从列表中选择
 *
 * @author Edgar  Date 2016/8/5
 */
class RoundRobinStrategy implements SelectStrategy {

  private final AtomicInteger integer = new AtomicInteger(0);

  @Override
  public Record select(List<Record> records) {
    if (records == null || records.isEmpty()) {
      return null;
    }
    int index = Math.abs(integer.getAndIncrement());
    return records.get(index % records.size());
  }

}