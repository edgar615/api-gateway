package com.github.edgar615.gateway.plugin.user;

import com.github.edgar615.gateway.core.definition.ApiPlugin;
import com.github.edgar615.gateway.core.definition.ApiPluginFactory;
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

        UserRestriction plugin = new UserRestrictionImpl();
        whitelist.forEach(w -> plugin.addWhitelist(w));
        blacklist.forEach(b -> plugin.addBlacklist(b));
        return plugin;
    }

    @Override
    public JsonObject encode(ApiPlugin plugin) {
        UserRestriction userRestriction = (UserRestriction) plugin;
        return new JsonObject().put("user.restriction", new JsonObject()
                .put("whitelist", new JsonArray(userRestriction.whitelist()))
                .put("blacklist", new JsonArray(userRestriction.blacklist())));
    }

    @Override
    public String name() {
        return UserRestriction.class.getSimpleName();
    }

    @Override
    public ApiPlugin create() {
        return new UserRestrictionImpl();
    }
}
