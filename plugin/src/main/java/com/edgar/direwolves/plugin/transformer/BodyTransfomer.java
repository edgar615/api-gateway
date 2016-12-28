package com.edgar.direwolves.plugin.transformer;

import java.util.List;
import java.util.Map;

/**
 * body的转换规则.
 *
 * @author Edgar  Date 2016/10/18
 */
public interface BodyTransfomer {

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
}
