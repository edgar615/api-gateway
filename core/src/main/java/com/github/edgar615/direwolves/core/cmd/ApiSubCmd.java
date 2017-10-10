package com.github.edgar615.direwolves.core.cmd;

import com.github.edgar615.direwolves.core.definition.ApiDefinition;
import io.vertx.core.json.JsonObject;

/**
 * API的子命令接口，主要用于定义插件的命令.
 * 例如下面的命令表示给 device.get的接口增加一个黑名单.
 * <Pre>
 * cmd : api.plugin
 * name: device.get
 *subcmd : ip.blacklist.add
 * ip : 10.4.7.15
 * </Pre>
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
