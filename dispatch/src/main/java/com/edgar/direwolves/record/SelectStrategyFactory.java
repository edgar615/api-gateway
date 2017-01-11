package com.edgar.direwolves.record;

/**
 * SelectStrategy的工厂接口
 *
 * @author Edgar  Date 2016/8/5
 */
public interface SelectStrategyFactory {

  /**
   * @return 策略名称.
   */
  String name();

  /**
   * @return SelectStrategy
   */
  SelectStrategy create();

}
