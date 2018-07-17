package com.github.edgar615.gateway.core.cmd;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * API的命令接口
 *
 * @author Edgar  Date 2017/1/19
 */
public interface ApiCmd {
  Logger LOGGER = LoggerFactory.getLogger(ApiCmd.class);

  /**
   * @return 命令名称.
   */
  String cmd();

  Future<JsonObject> doHandle(JsonObject jsonObject);

  /**
   * 处理命令
   *
   * @param jsonObject 参数
   * @return 处理结果
   */
  default Future<JsonObject> handle(JsonObject jsonObject) {
    try {
      Future<JsonObject> future = doHandle(jsonObject);
      return future;
    } catch (Exception e) {
      return Future.failedFuture(e);
    }
  }

  default JsonObject succeedResult() {
    return new JsonObject()
        .put("result", 1);
  }

  default void setConfig(JsonObject config, JsonObject privateConfig) {
    if (config.containsKey("publishedAddress")) {
      privateConfig.put("publishedAddress", config.getString("publishedAddress"));
    }
    if (config.containsKey("unpublishedAddress")) {
      privateConfig.put("unpublishedAddress", config.getString("unpublishedAddress"));
    }
  }
}
