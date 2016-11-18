package com.edgar.direwolves.plugin.acl;

import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * ACL控制策略的工厂类.
 * json配置:
 * <pre>
 * "acl_restriction" : {
 *    "whitelist" : ["group1", "group2"],
 *    "blacklist" : ["guest"]
 * }
 * </pre>
 *
 * @author Edgar  Date 2016/10/21
 */
public class AclRestrictionFactory implements ApiPluginFactory<AclRestrictionPlugin> {
  @Override
  public AclRestrictionPlugin decode(JsonObject jsonObject) {
    if (!jsonObject.containsKey("acl_restriction")) {
      return null;
    }
    JsonObject args = jsonObject.getJsonObject("acl_restriction", new JsonObject());
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
  public JsonObject encode(AclRestrictionPlugin plugin) {
    return new JsonObject().put("acl_restriction", new JsonObject()
            .put("whitelist", new JsonArray(plugin.whitelist()))
            .put("blacklist", new JsonArray(plugin.blacklist())));
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
