package com.github.edgar615.direwolves.plugin.scope;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.github.edgar615.direwolves.core.cmd.ApiSubCmd;
import com.github.edgar615.direwolves.core.definition.ApiDefinition;
import com.github.edgar615.util.validation.Rule;
import com.github.edgar615.util.validation.Validations;
import io.vertx.core.json.JsonObject;

/**
 * 设置鉴权的命令
 *
 * 命令字:scope.add
 * 参数 scope:权限值
 *
 * @author Edgar  Date 2017/1/20
 */
public class AddScopeCmd implements ApiSubCmd {

  private final Multimap<String, Rule> rules = ArrayListMultimap.create();

  public AddScopeCmd() {
    rules.put("scope", Rule.required());
  }

  @Override
  public String cmd() {
    return "scope.add";
  }

  @Override
  public void handle(ApiDefinition definition, JsonObject jsonObject) {
    Validations.validate(jsonObject.getMap(), rules);
    String scope = jsonObject.getString("scope");
    ScopePlugin plugin = ScopePlugin.create(scope);
    definition.addPlugin(plugin);

  }

}
