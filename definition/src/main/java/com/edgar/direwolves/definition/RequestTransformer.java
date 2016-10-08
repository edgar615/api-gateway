package com.edgar.direwolves.definition;

import java.util.List;
import java.util.Map;

/**
 * Created by Edgar on 2016/10/8.
 *
 * @author Edgar  Date 2016/10/8
 */
public interface RequestTransformer {

  /**
   * @return body参数的替换规则.
   */
  List<Map.Entry<String, String>> reqBodyArgsReplace();

  /**
   * @return body参数的增加规则
   */
  List<Map.Entry<String, String>> reqBodyArgsAdd();

  /**
   * @return body参数的删除规则
   */
  List<String> reqBodyArgsRemove();

  /**
   * @return url参数的替换规则
   */
  List<Map.Entry<String, String>> reqUrlArgsReplace();

  /**
   * @return url参数的增加规则
   */
  List<Map.Entry<String, String>> reqUrlArgsAdd();

  /**
   * @return url参数的删除规则
   */
  List<String> reqUrlArgsRemove();

  /**
   * @return header的替换规则
   */
  List<Map.Entry<String, String>> reqHeadersReplace();

  /**
   * @return header的新增规则
   */
  List<Map.Entry<String, String>> reqHeadersAdd();

  /**
   * @return header的删除规则
   */
  List<String> reqHeadersRemove();
}
