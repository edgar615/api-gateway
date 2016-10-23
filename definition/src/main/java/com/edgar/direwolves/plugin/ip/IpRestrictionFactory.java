package com.edgar.direwolves.plugin.ip;

import com.google.common.base.Preconditions;

import com.edgar.direwolves.plugin.ApiPlugin;
import com.edgar.direwolves.plugin.ApiPluginFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Edgar on 2016/10/21.
 *
 * @author Edgar  Date 2016/10/21
 */
public class IpRestrictionFactory implements ApiPluginFactory<IpRestriction> {
  @Override
  public IpRestriction decode(JsonObject jsonObject) {
    JsonObject config = jsonObject.getJsonObject("ip_restriction", new JsonObject());
    JsonArray whiteArray = config.getJsonArray("whitelist", new JsonArray());
    JsonArray blackArray = config.getJsonArray("blacklist", new JsonArray());
    List<String> whitelist = new ArrayList<>();
    List<String> blacklist = new ArrayList<>();
    for (int i = 0; i < whiteArray.size(); i++) {
      whitelist.add(whiteArray.getString(i));
    }
    for (int i = 0; i < blackArray.size(); i++) {
      blacklist.add(blackArray.getString(i));
    }

    IpRestriction aclRestriction = new IpRestrictionImpl();
    whitelist.forEach(w -> aclRestriction.addWhitelist(w));
    blacklist.forEach(b -> aclRestriction.addBlacklist(b));
    return aclRestriction;
  }

  @Override
  public JsonObject encode(IpRestriction ipRestriction) {
    return new JsonObject().put("ip_restriction", new JsonObject()
        .put("whitelist", new JsonArray(ipRestriction.whitelist()))
        .put("blacklist", new JsonArray(ipRestriction.blacklist())));
  }

  @Override
  public String name() {
    return "IP_RESTRICTION";
  }

  @Override
  public ApiPlugin create() {
    return new IpRestrictionImpl();
  }
}
