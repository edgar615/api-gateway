package com.edgar.direwolves.definition;

import com.google.common.base.Preconditions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 将JsonObject转换为RateLimitDefinition的集合.
 *
 * @author Edgar  Date 2016/9/13
 */
public class JsonToRateLimitDefinition implements Function<JsonObject, List<RateLimitDefinition>> {
    private static final JsonToRateLimitDefinition INSTANCE = new JsonToRateLimitDefinition();

    private JsonToRateLimitDefinition() {
    }

    public static Function<JsonObject, List<RateLimitDefinition>> instance() {
        return INSTANCE;
    }

    @Override
    public List<RateLimitDefinition> apply(JsonObject jsonObject) {
        List<RateLimitDefinition> definitions = new ArrayList<>();
        ;
        Preconditions.checkArgument(jsonObject.containsKey("name"), "api name cannot be null");

        if (!jsonObject.containsKey("rate_limit")) {
            return definitions;
        }

        String name = jsonObject.getString("name");
        JsonArray rateLimitArray = jsonObject.getJsonArray("rate_limit");
        for (int i = 0; i < rateLimitArray.size(); i++) {
            JsonObject rateLimit = rateLimitArray.getJsonObject(i);

            String type = rateLimit.getString("type");
            String limitBy = rateLimit.getString("limit_by");
            int limit = rateLimit.getInteger("limit");

            RateLimitBy rateLimitBy = createRateLimitBy(limitBy);
            RateLimitType rateLimitType = createRateLimitType(type);


            if (rateLimitBy != null && rateLimitType != null) {
                definitions.add(RateLimitDefinition.create(name, rateLimitBy, rateLimitType, limit));
            }
        }

        return definitions;
    }

    private RateLimitBy createRateLimitBy(String value) {
        try {
            return RateLimitBy.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private RateLimitType createRateLimitType(String value) {
        try {
            return RateLimitType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
