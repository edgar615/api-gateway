package com.edgar.direwolves.record;

import java.util.Random;

/**
 * RandomStrategy的工厂类.
 *
 * @author Edgar  Date 2016/8/5
 */
public class RoundRobinStrategyFactory implements SelectStrategyFactory {

  @Override
  public String name() {
    return "round_robin";
  }

  @Override
  public SelectStrategy create() {
    return new RoundRobinStrategy();
  }


}