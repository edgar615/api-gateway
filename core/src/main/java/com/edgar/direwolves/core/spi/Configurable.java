package com.edgar.direwolves.core.spi;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * 配置接口.
 * 凡是继承该接口的类再创建之后都应该执行config方法，执行初始化工作.
 * Created by edgar on 16-9-20.
 */
public interface Configurable {
  /**
   * 配置方法.
   *
   * @param vertx  Vertx
   * @param config 配置
   */
  void config(Vertx vertx, JsonObject config);
}
