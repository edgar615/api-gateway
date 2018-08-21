package com.github.edgar615.gateway.core.definition;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.function.Function;

/**
 * 将ApiDefinition转换为JsonObject.
 * Created by edgar on 16-9-13.
 */
class ApiDefinitionEncoder implements Function<ApiDefinition, JsonObject> {

    private static final ApiDefinitionEncoder INSTANCE = new ApiDefinitionEncoder();

    private ApiDefinitionEncoder() {
    }

    static Function<ApiDefinition, JsonObject> instance() {
        return INSTANCE;
    }

    @Override
    public JsonObject apply(ApiDefinition definition) {
        JsonObject jsonObject = new JsonObject()
                .put("name", definition.name())
                .put("method", definition.method().name())
                .put("path", definition.path())
                .put("endpoints", createEndpointArray(definition.endpoints()));
        if (definition instanceof AntPathApiDefinition) {
            AntPathApiDefinition antPathApiDefinition = (AntPathApiDefinition) definition;
            jsonObject.put("type", "ant");
            jsonObject.put("ignoredPatterns", antPathApiDefinition.ignoredPatterns());
        }
        if (definition instanceof RegexPathApiDefinition) {
            jsonObject.put("type", "regex");
        }
        definition.plugins().forEach(p -> jsonObject.mergeIn(p.encode()));
        return jsonObject;

    }

    private JsonArray createEndpointArray(List<Endpoint> endpoints) {
        JsonArray jsonArray = new JsonArray();
        endpoints.forEach(endpoint -> {
            jsonArray.add(Endpoints.toJson(endpoint));
        });
        return jsonArray;
    }


}
