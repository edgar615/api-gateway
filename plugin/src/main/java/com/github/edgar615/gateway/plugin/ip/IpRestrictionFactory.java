package com.github.edgar615.gateway.plugin.ip;

import com.github.edgar615.gateway.core.definition.ApiPlugin;
import com.github.edgar615.gateway.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * IP控制策略的工厂类.
 *
 * @author Edgar  Date 2016/10/21
 */
public class IpRestrictionFactory implements ApiPluginFactory {
    @Override
    public ApiPlugin decode(JsonObject jsonObject) {
        if (!jsonObject.containsKey("ip.restriction")) {
            return null;
        }
        JsonObject config = jsonObject.getJsonObject("ip.restriction", new JsonObject());
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
    public JsonObject encode(ApiPlugin plugin) {
        IpRestriction ipRestriction = (IpRestriction) plugin;
        return new JsonObject().put("ip.restriction", new JsonObject()
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
