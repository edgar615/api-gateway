package com.edgar.direwolves.core.spi;

import io.vertx.core.json.JsonObject;

/**
 * 对象与JsonObject之间的转换.
 *
 * @author Edgar  Date 2016/10/21
 */
public interface JsonObjectCodec<T> {

  /**
   * 将Json转换为对象 .
   *
   * @param jsonObject json
   * @return 对象
   */
  T decode(JsonObject jsonObject);

  /**
   * 将一个对象转换为json
   *
   * @param obj 需要转换的对象.
   * @return json
   */
  JsonObject encode(T obj);
}
