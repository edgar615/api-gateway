package com.github.edgar615.direwolves.plugin.appkey;

import com.github.edgar615.direwolves.core.definition.ApiPlugin;
import com.github.edgar615.direwolves.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * AppKey控制策略的工厂类.
 *
 * @author Edgar  Date 2016/10/21
 */
public class AppKeyRestrictionFactory implements ApiPluginFactory {
  @Override
  public ApiPlugin decode(JsonObject jsonObject) {
    if (!jsonObject.containsKey("appKey.restriction")) {
      return null;
    }
    JsonObject config = jsonObject.getJsonObject("appKey.restriction", new JsonObject());
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

    AppKeyRestriction aclRestriction = new AppKeyRestrictionImpl();
    whitelist.forEach(w -> aclRestriction.addWhitelist(w));
    blacklist.forEach(b -> aclRestriction.addBlacklist(b));
    return aclRestriction;
  }

  @Override
  public JsonObject encode(ApiPlugin plugin) {
    AppKeyRestriction appKeyRestriction = (AppKeyRestriction) plugin;
    return new JsonObject().put("appKey.restriction", new JsonObject()
            .put("whitelist", new JsonArray(appKeyRestriction.whitelist()))
            .put("blacklist", new JsonArray(appKeyRestriction.blacklist())));
  }

  @Override
  public String name() {
    return AppKeyRestriction.class.getSimpleName();
  }

  @Override
  public ApiPlugin create() {
    return new AppKeyRestrictionImpl();
  }
}
