package com.edgar.direwolves.service;

import io.vertx.servicediscovery.Record;

import java.util.List;
import java.util.Random;

/**
 * 从列表中随机选择一个
 *
 * @author Edgar  Date 2016/8/5
 */
class RandomStrategy implements SelectStrategy {

  private final Random random = new Random();

  @Override
  public Record select(List<Record> records) {
    if (records == null || records.isEmpty()) {
      return null;
    }
    int index = random.nextInt(records.size());
    return records.get(index);
  }

}