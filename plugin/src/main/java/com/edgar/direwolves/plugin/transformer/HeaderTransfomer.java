package com.edgar.direwolves.plugin.transformer;

import java.util.List;
import java.util.Map;

/**
 * header的转换规则.
 *
 * @author Edgar  Date 2016/10/18
 */
public interface HeaderTransfomer {

  /**
   * @return header的替换规则
   */
  List<Map.Entry<String, String>> headerReplaced();

  /**
   * @return header的新增规则
   */
  List<Map.Entry<String, String>> headerAdded();

  /**
   * @return header的删除规则
   */
  List<String> headerRemoved();

  /**
   * 增加一个header
   *
   * @param key
   * @param value
   * @return
   */
  HeaderTransfomer addHeader(String key, String value);

  /**
   * 删除一个header
   *
   * @param key
   * @return
   */
  HeaderTransfomer removeHeader(String key);

  /**
   * 替换一个header,只有当header存在时才替换;
   *
   * @param key
   * @param value
   * @return
   */
  HeaderTransfomer replaceHeader(String key, String value);

}
