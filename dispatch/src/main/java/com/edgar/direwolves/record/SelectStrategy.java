package com.edgar.direwolves.record;

import com.google.common.collect.Lists;

import io.vertx.servicediscovery.Record;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * 从一组Record中提取出一个Record的接口.
 *
 * @author Edgar  Date 2016/8/5
 */
public interface SelectStrategy {

  List<SelectStrategyFactory> factories
          = Lists.newArrayList(ServiceLoader.load(SelectStrategyFactory.class));

  /**
   * 从一组records中取出一个record
   *
   * @return
   */
  Record select(List<Record> records);

  static SelectStrategy create(String name) {
    List<SelectStrategyFactory> factoryList =
            factories.stream()
                    .filter(f -> f.name().equalsIgnoreCase(name))
                    .collect(Collectors.toList());
    if (factoryList.isEmpty()) {
      return null;
    }
    return factoryList.get(0).create();
  }
}
