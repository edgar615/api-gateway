package com.edgar.direwolves.plugin.authorization;

import com.edgar.direwolves.core.cmd.ApiSubCmd;
import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.util.validation.Rule;
import com.edgar.util.validation.Validations;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.vertx.core.json.JsonObject;

/**
 * 设置鉴权的命令
 *
 * 命令字:authorise.add
 * 参数 scope:权限值
 *
 * @author Edgar  Date 2017/1/20
 */
public class AddAuthoriseCmd implements ApiSubCmd {

  private final Multimap<String, Rule> rules = ArrayListMultimap.create();

  public AddAuthoriseCmd() {
    rules.put("scope", Rule.required());
  }

  @Override
  public String cmd() {
    return "authorise.add";
  }

  @Override
  public void handle(ApiDefinition definition, JsonObject jsonObject) {
    Validations.validate(jsonObject.getMap(), rules);
    String scope = jsonObject.getString("scope");
    AuthorisePlugin plugin = AuthorisePlugin.create(scope);
    definition.addPlugin(plugin);

  }

}
