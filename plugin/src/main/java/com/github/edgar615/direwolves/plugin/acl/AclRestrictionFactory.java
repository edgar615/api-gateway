package com.github.edgar615.direwolves.plugin.acl;

import com.github.edgar615.direwolves.core.definition.ApiPlugin;
import com.github.edgar615.direwolves.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * ACL控制策略的工厂类.
 *
 * @author Edgar  Date 2016/10/21
 */
public class AclRestrictionFactory implements ApiPluginFactory {
  @Override
  public ApiPlugin decode(JsonObject jsonObject) {
    if (!jsonObject.containsKey("acl.restriction")) {
      return null;
    }
    JsonObject args = jsonObject.getJsonObject("acl.restriction", new JsonObject());
    JsonArray whiteArray = args.getJsonArray("whitelist", new JsonArray());
    JsonArray blackArray = args.getJsonArray("blacklist", new JsonArray());
    List<String> whitelist = new ArrayList<>();
    List<String> blacklist = new ArrayList<>();
    for (int i = 0; i < whiteArray.size(); i++) {
      whitelist.add(whiteArray.getString(i));
    }
    for (int i = 0; i < blackArray.size(); i++) {
      blacklist.add(blackArray.getString(i));
    }

    AclRestriction plugin = new AclRestrictionImpl();
    whitelist.forEach(w -> plugin.addWhitelist(w));
    blacklist.forEach(b -> plugin.addBlacklist(b));
    return plugin;
  }

  @Override
  public JsonObject encode(ApiPlugin plugin) {
    AclRestriction aclRestriction = (AclRestriction) plugin;
    return new JsonObject().put("acl.restriction", new JsonObject()
            .put("whitelist", new JsonArray(aclRestriction.whitelist()))
            .put("blacklist", new JsonArray(aclRestriction.blacklist())));
  }

  @Override
  public String name() {
    return AclRestriction.class.getSimpleName();
  }

  @Override
  public ApiPlugin create() {
    return new AclRestrictionImpl();
  }
}
