package com.edgar.direwolves.plugin.acl;

import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.ApiPluginFactory;
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

    AclRestrictionPlugin plugin = new AclRestrictionPluginImpl();
    whitelist.forEach(w -> plugin.addWhitelist(w));
    blacklist.forEach(b -> plugin.addBlacklist(b));
    return plugin;
  }

  @Override
  public JsonObject encode(ApiPlugin plugin) {
    AclRestrictionPlugin aclRestrictionPlugin = (AclRestrictionPlugin) plugin;
    return new JsonObject().put("acl.restriction", new JsonObject()
            .put("whitelist", new JsonArray(aclRestrictionPlugin.whitelist()))
            .put("blacklist", new JsonArray(aclRestrictionPlugin.blacklist())));
  }

  @Override
  public String name() {
    return AclRestrictionPlugin.class.getSimpleName();
  }

  @Override
  public ApiPlugin create() {
    return new AclRestrictionPluginImpl();
  }
}
