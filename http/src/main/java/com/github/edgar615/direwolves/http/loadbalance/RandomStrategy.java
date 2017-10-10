package com.github.edgar615.direwolves.http.loadbalance;

import io.vertx.servicediscovery.Record;

import java.util.List;
import java.util.Random;

/**
 * 从给定列表中随机选择一个对象.
 * Created by edgar on 17-5-6.
 */
class RandomStrategy implements ChooseStrategy {
  private final Random random = new Random();

  @Override
  public Record get(List<Record> instances) {
    if (instances == null || instances.isEmpty()) {
      return null;
    }
    int index = random.nextInt(instances.size());
    return instances.get(index);
  }

}
