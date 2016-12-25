package com.edgar.direwolves.plugin.ip;

import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * IP控制策略的工厂类.
 * json配置:
 * <pre>
 * "ip_restriction" : {
 *    "whitelist" : ["192.168.0.1", "10.4.7.*"],
 *    "blacklist" : ["192.168.0.100"]
 * }
 * </pre>
 *
 * @author Edgar  Date 2016/10/21
 */
public class IpRestrictionFactory implements ApiPluginFactory<IpRestriction> {
  @Override
  public IpRestriction decode(JsonObject jsonObject) {
    if (!jsonObject.containsKey("ip_restriction")) {
      return null;
    }
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
    return IpRestriction.class.getSimpleName();
  }

  @Override
  public ApiPlugin create() {
    return new IpRestrictionImpl();
  }
}
