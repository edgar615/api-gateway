package com.github.edgar615.direwolves.plugin.transformer;

import java.util.List;
import java.util.Map;

/**
 * body的转换规则.
 *
 * @author Edgar  Date 2016/10/18
 */
public interface BodyTransfomer {

  /**
   * @return body的替换规则.
   */
  List<Map.Entry<String, String>> bodyReplaced();

  /**
   * @return body的增加规则
   */
  List<Map.Entry<String, String>> bodyAdded();

  /**
   * @return body的删除规则
   */
  List<String> bodyRemoved();

  /**
   * 增加一个body
   *
   * @param key
   * @param value
   * @return
   */
  BodyTransfomer addBody(String key, String value);

  /**
   * 删除一个body
   *
   * @param key
   * @return
   */
  BodyTransfomer removeBody(String key);

  /**
   * 替换一个body,只有当body存在时才替换;
   *
   * @param key
   * @param value
   * @return
   */
  BodyTransfomer replaceBody(String key, String value);
}
