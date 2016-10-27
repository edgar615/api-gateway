package com.edgar.direwolves.plugin.arg;

import java.util.List;

/**
 * Created by edgar on 16-10-23.
 */
public interface ArgPlugin {

  /**
   * @return URL参数
   */
  List<Parameter> parameters();

  /**
   * 增加一个url参数
   *
   * @param parameter 参数
   * @return ArgPlugin
   */
  ArgPlugin add(Parameter parameter);

  /**
   * 删除一个url参数
   *
   * @param name 参数名
   * @return ArgPlugin
   */
  ArgPlugin remove(String name);

  /**
   * @param name 参数名
   * @return　参数
   */
  Parameter parameter(String name);

  /**
   * 删除所有参数.
   *
   * @return ArgPlugin
   */
  ArgPlugin clear();
}
