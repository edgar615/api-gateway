package com.edgar.direwolves.core.cmd;

import com.edgar.direwolves.core.definition.ApiDefinition;
import io.vertx.core.json.JsonObject;

/**
 * API的子命令接口，主要用于插件的命令
 *
 * @author Edgar  Date 2017/1/19
 */
public interface ApiSubCmd {

  /**
   * @return 命令名称.
   */
  String cmd();

  /**
   * 处理命令
   *
   * @param definition API定义
   * @param jsonObject 参数
   */
  void handle(ApiDefinition definition, JsonObject jsonObject);

}
