package com.edgar.direwolves.definition;

import com.google.common.base.Preconditions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 将JsonObject转换为AuthDefinition的集合.
 *
 * @author Edgar  Date 2016/9/13
 */
class JsonToAuthDefinitions implements Function<JsonObject, List<AuthDefinition>> {
    private static final JsonToAuthDefinitions INSTANCE = new JsonToAuthDefinitions();

    private JsonToAuthDefinitions() {
    }

    public static Function<JsonObject, List<AuthDefinition>> instance() {
        return INSTANCE;
    }

    @Override
    public List<AuthDefinition> apply(JsonObject jsonObject) {
        List<AuthDefinition> authDefinitions = new ArrayList<>();;
        Preconditions.checkArgument(jsonObject.containsKey("name"), "api name cannot be null");

        if (!jsonObject.containsKey("name")) {
            return authDefinitions;
        }

        String name = jsonObject.getString("name");
        Object value = jsonObject.getValue("auth");
        if (value instanceof String) {
            if (value instanceof String) {
                AuthType authType = createAuthType((String) value);
                if (authType != null) {
                    authDefinitions.add(AuthDefinition.create(name, authType));
                }
            }
        }
        if (value instanceof JsonArray) {
            JsonArray authArray = jsonObject.getJsonArray("auth");
            for (int i = 0; i < authArray.size(); i ++) {
                String authTypeStr = authArray.getString(i);
                AuthType authType = createAuthType(authTypeStr);
                if (authType != null) {
                    authDefinitions.add(AuthDefinition.create(name, authType));
                }
            }
        }

        return authDefinitions;
    }

    private AuthType createAuthType(String value) {
        try {
            return AuthType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
