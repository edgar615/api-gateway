package com.edgar.direwolves.definition;

import java.util.List;
import java.util.Map;

/**
 * 参数的的转换规则.
 *
 * @author Edgar  Date 2016/10/18
 */
public interface ParamTransfomer {
  /**
   * @return param的替换规则
   */
  List<Map.Entry<String, String>> paramReplaced();

  /**
   * @return param的新增规则
   */
  List<Map.Entry<String, String>> paramAdded();

  /**
   * @return param的删除规则
   */
  List<String> paramRemoved();

  /**
   * 增加一个param
   *
   * @param key
   * @param value
   */
  void addParam(String key, String value);

  /**
   * 替换一个param,只有当param存在时才替换;
   *
   * @param key
   * @param value
   */
  void replaceParam(String key, String value);

  /**
   * 删除一个param
   *
   * @param key
   */
  void removeParam(String key);

}
