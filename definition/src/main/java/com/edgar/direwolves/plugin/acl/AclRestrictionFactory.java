package com.edgar.direwolves.plugin.acl;

import com.edgar.direwolves.plugin.ApiPlugin;
import com.edgar.direwolves.plugin.ApiPluginFactory;
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
public class AclRestrictionFactory implements ApiPluginFactory<AclRestriction> {
  @Override
  public AclRestriction decode(JsonObject jsonObject) {
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

    AclRestriction aclRestriction = new AclRestrictionImpl();
    whitelist.forEach(w -> aclRestriction.addWhitelist(w));
    blacklist.forEach(b -> aclRestriction.addBlacklist(b));
    return aclRestriction;
  }

  @Override
  public JsonObject encode(AclRestriction aclRestriction) {
    return new JsonObject().put("acl_restriction", new JsonObject()
        .put("whitelist", new JsonArray(aclRestriction.whitelist()))
        .put("blacklist", new JsonArray(aclRestriction.blacklist())));
  }

  @Override
  public String name() {
    return "ACL_RESTRICTION";
  }

  @Override
  public ApiPlugin create() {
    return new AclRestrictionImpl();
  }
}
