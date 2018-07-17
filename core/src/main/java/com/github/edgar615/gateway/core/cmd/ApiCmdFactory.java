package com.github.edgar615.gateway.core.cmd;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/1/19.
 *
 * @author Edgar  Date 2017/1/19
 */
public interface ApiCmdFactory {

  /**
   * 创建一个ApiCmd
   *
   * @param vertx
   * @param config
   * @return
   */
  ApiCmd create(Vertx vertx, JsonObject config);
}
