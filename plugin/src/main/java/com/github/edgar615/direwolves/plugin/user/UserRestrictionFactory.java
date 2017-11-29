package com.github.edgar615.direwolves.plugin.user;

import com.github.edgar615.direwolves.core.definition.ApiPlugin;
import com.github.edgar615.direwolves.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * User控制策略的工厂类.
 *
 * @author Edgar  Date 2016/10/21
 */
public class UserRestrictionFactory implements ApiPluginFactory {
  @Override
  public ApiPlugin decode(JsonObject jsonObject) {
    if (!jsonObject.containsKey("user.restriction")) {
      return null;
    }
    JsonObject args = jsonObject.getJsonObject("user.restriction", new JsonObject());
    JsonArray whiteArray = args.getJsonArray("whitelist", new JsonArray());
    JsonArray blackArray = args.getJsonArray("blacklist", new JsonArray());
    List<String> whitelist = new ArrayList<>();
    List<String> blacklist = new ArrayList<>();
    for (int i = 0; i < whiteArray.size(); i++) {
      whitelist.add(whiteArray.getValue(i).toString());
    }
    for (int i = 0; i < blackArray.size(); i++) {
      blacklist.add(blackArray.getValue(i).toString());
    }

    UserRestrictionPlugin plugin = new UserRestrictionPluginImpl();
    whitelist.forEach(w -> plugin.addWhitelist(w));
    blacklist.forEach(b -> plugin.addBlacklist(b));
    return plugin;
  }

  @Override
  public JsonObject encode(ApiPlugin plugin) {
    UserRestrictionPlugin userRestrictionPlugin = (UserRestrictionPlugin) plugin;
    return new JsonObject().put("user.restriction", new JsonObject()
            .put("whitelist", new JsonArray(userRestrictionPlugin.whitelist()))
            .put("blacklist", new JsonArray(userRestrictionPlugin.blacklist())));
  }

  @Override
  public String name() {
    return UserRestrictionPlugin.class.getSimpleName();
  }

  @Override
  public ApiPlugin create() {
    return new UserRestrictionPluginImpl();
  }
}
