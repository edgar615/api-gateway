package com.edgar.direwolves.definition;

import com.edgar.util.validation.Rule;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * 将JsonObject转换为IpRestrictionDefinition.
 *
 * @author Edgar  Date 2016/9/13
 */
public class JsonToIpRestrictionDefinition implements Function<JsonObject, IpRestrictionDefinition> {
    private static final JsonToIpRestrictionDefinition INSTANCE = new JsonToIpRestrictionDefinition();

    private JsonToIpRestrictionDefinition() {
    }

    public static Function<JsonObject, IpRestrictionDefinition> instance() {
        return INSTANCE;
    }

    @Override
    public IpRestrictionDefinition apply(JsonObject jsonObject) {
        Preconditions.checkArgument(jsonObject.containsKey("name"), "api name cannot be null");
        if (!jsonObject.containsKey("ip_restriction")) {
            return null;
        }
        String name = jsonObject.getString("name");
        JsonObject ipRestriction = jsonObject.getJsonObject("ip_restriction");
        JsonArray whiteArray = ipRestriction.getJsonArray("whitelist", new JsonArray());
        JsonArray blackArray = ipRestriction.getJsonArray("blacklist", new JsonArray());
        List<String> whitelist = new ArrayList<>();
        List<String> blacklist = new ArrayList<>();
        for (int i = 0; i < whiteArray.size(); i ++) {
            whitelist.add(whiteArray.getString(i));
        }
        for (int i = 0; i < blackArray.size(); i ++) {
            blacklist.add(blackArray.getString(i));
        }

        IpRestrictionDefinition definition = IpRestrictionDefinition.create(name);
        whitelist.forEach(whiteIp -> definition.addWhitelist(whiteIp));
        blacklist.forEach(blackIp -> definition.addBlacklist(blackIp));

        return definition;
    }

}
