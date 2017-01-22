package com.edgar.direwolves.cmd;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.cmd.ApiSubCmd;
import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.util.validation.Rule;
import com.edgar.util.validation.Validations;
import io.vertx.core.json.JsonObject;

/**
 * 删除插件的子命令
 * 命令字：delete
 * 参数: plugin插件名称，必填项
 *
 * @author Edgar  Date 2017/1/22
 */
public class DeletePluginCmd implements ApiSubCmd {

  private final Multimap<String, Rule> rules = ArrayListMultimap.create();

  public DeletePluginCmd() {
    rules.put("plugin", Rule.required());
  }

  @Override
  public String cmd() {
    return "delete";
  }

  @Override
  public void handle(ApiDefinition definition, JsonObject jsonObject) {
    Validations.validate(jsonObject.getMap(), rules);
    String plugin = jsonObject.getString("plugin");
    definition.removePlugin(plugin);
  }
}
