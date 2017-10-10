package com.github.edgar615.direwolves.plugin.ip;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.github.edgar615.direwolves.core.cmd.ApiSubCmd;
import com.github.edgar615.direwolves.core.definition.ApiDefinition;
import com.github.edgar615.util.validation.Rule;
import com.github.edgar615.util.validation.Validations;
import io.vertx.core.json.JsonObject;

/**
 * 增加IP黑名单的命令
 *
 * @author Edgar  Date 2017/1/20
 */
public class AddBlacklistCmd implements ApiSubCmd {

  private final Multimap<String, Rule> rules = ArrayListMultimap.create();

  public AddBlacklistCmd() {
    rules.put("ip", Rule.required());
  }

  @Override
  public String cmd() {
    return "ip.blacklist.add";
  }

  @Override
  public void handle(ApiDefinition definition, JsonObject jsonObject) {
    Validations.validate(jsonObject.getMap(), rules);
    String ip = jsonObject.getString("ip");
    IpRestriction ipRestriction =
            (IpRestriction) definition.plugin(IpRestriction.class.getSimpleName());
    if (ipRestriction != null) {
      ipRestriction.addBlacklist(ip);
    } else {
      ipRestriction = IpRestriction.create();
      ipRestriction.addBlacklist(ip);
      definition.addPlugin(ipRestriction);
    }

  }

}
