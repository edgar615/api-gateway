package com.edgar.direwolves.record;

/**
 * RandomStrategy的工厂类.
 *
 * @author Edgar  Date 2016/8/5
 */
public class RandomStrategyFactory implements SelectStrategyFactory {

  @Override
  public String name() {
    return "random";
  }

  @Override
  public SelectStrategy create() {
    return new RandomStrategy();
  }


}