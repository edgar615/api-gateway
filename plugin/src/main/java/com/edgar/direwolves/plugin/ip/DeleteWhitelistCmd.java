package com.edgar.direwolves.plugin.ip;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.cmd.ApiSubCmd;
import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.util.validation.Rule;
import com.edgar.util.validation.Validations;
import io.vertx.core.json.JsonObject;

/**
 * 删除IP白名单的命令
 *
 * @author Edgar  Date 2017/1/20
 */
public class DeleteWhitelistCmd implements ApiSubCmd {

  private final Multimap<String, Rule> rules = ArrayListMultimap.create();

  public DeleteWhitelistCmd() {
    rules.put("ip", Rule.required());
  }

  @Override
  public String cmd() {
    return "ip.whitelist.delete";
  }

  @Override
  public void handle(ApiDefinition definition, JsonObject jsonObject) {
    Validations.validate(jsonObject.getMap(), rules);
    String ip = jsonObject.getString("ip");
    IpRestriction ipRestriction =
            (IpRestriction) definition.plugin(IpRestriction.class.getSimpleName());
    if (ipRestriction != null) {
      ipRestriction.removeWhitelist(ip);
    }

  }

}
