package com.edgar.direwolves.core.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import java.util.List;

/**
 * Created by Edgar on 2017/1/5.
 *
 * @author Edgar  Date 2017/1/5
 */
public class MultimapUtils {

  private MultimapUtils() {
    throw new AssertionError("Not instantiable: " + MultimapUtils.class);
  }

  /**
   * 获取Multimap中的第一个参数.
   *
   * @param multimap    参数列表
   * @param key 参数名
   * @return 参数值
   */
  public static String getFirst(Multimap<String, String> multimap, String key) {
    List<String> values = Lists.newArrayList(multimap.get(key));
    if (values.isEmpty()) {
      return null;
    }
    return values.get(0);
  }
}
