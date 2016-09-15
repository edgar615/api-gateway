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
 * 将JsonObject转换为ApiDefinition.
 *
 * @author Edgar  Date 2016/9/13
 */
public class JsonToApiDefinition implements Function<JsonObject, ApiDefinition> {
    private static final JsonToApiDefinition INSTANCE = new JsonToApiDefinition();

    private JsonToApiDefinition() {
    }

    public static Function<JsonObject, ApiDefinition> instance() {
        return INSTANCE;
    }

    @Override
    public ApiDefinition apply(JsonObject jsonObject) {
        Preconditions.checkArgument(jsonObject.containsKey("name"), "api name cannot be null");
        if (!jsonObject.containsKey("path")) {
            return null;
        }
        Preconditions.checkArgument(jsonObject.containsKey("path"), "api path cannot be null");
        Preconditions.checkArgument(jsonObject.containsKey("endpoints"), "api endpoints cannot be null");
        ApiDefinitionBuilder builder = ApiDefinition.builder();
        builder.setName(jsonObject.getString("name"));
        builder.setPath(jsonObject.getString("path"));
        builder.setScope(jsonObject.getString("scope", "default"));
        builder.setMethod(httpMethod(jsonObject));
        if (jsonObject.containsKey("url_args")) {
            builder.setUrlArgs(createParameterList(jsonObject.getJsonArray("url_args")));
        }
        if (jsonObject.containsKey("body_args")) {
            builder.setBodyArgs(createParameterList(jsonObject.getJsonArray("body_args")));
        }

        builder.setEndpoints(createEndpoints(jsonObject.getJsonArray("endpoints")));

        return builder.build();
    }

    private HttpMethod httpMethod(JsonObject jsonObject) {
        HttpMethod httpMethod = HttpMethod.GET;
        String method = jsonObject.getString("method", "GET");
        if ("GET".equalsIgnoreCase(method)) {
            httpMethod = HttpMethod.GET;
        }
        if ("DELETE".equalsIgnoreCase(method)) {
            httpMethod = HttpMethod.DELETE;
        }
        if ("POST".equalsIgnoreCase(method)) {
            httpMethod = HttpMethod.POST;
        }
        if ("PUT".equalsIgnoreCase(method)) {
            httpMethod = HttpMethod.PUT;
        }
        return httpMethod;
    }

    private List<Parameter> createParameterList(JsonArray args) {
        List<Parameter> parameters = new ArrayList<>(args.size());
        for (int i = 0; i < args.size(); i++) {
            JsonObject arg = args.getJsonObject(i);
            String argName = arg.getString("name");
            Preconditions.checkNotNull(argName, "arg name cannot be null");
            Object defaultValue = arg.getValue("defaut_value");
            Parameter parameter = new Parameter(argName, defaultValue);
            parameters.add(parameter);
            JsonObject rules = arg.getJsonObject("rules", new JsonObject());
            addRule(parameter, rules);
        }
        return parameters;
    }

    private void addRule(Parameter parameter, JsonObject rules) {
        rules.getMap().forEach((key, value) -> {
            if ("required".equals(key) &&
                    "true".equals(value.toString())) {
                parameter.addRule(Rule.required());
            }
            if ("max_length".equals(key)) {
                parameter.addRule(Rule.maxLength((Integer) value));
            }
            if ("min_length".equals(key)) {
                parameter.addRule(Rule.minLength((Integer) value));
            }
            if ("max".equals(key)) {
                parameter.addRule(Rule.max((Integer) value));
            }
            if ("min".equals(key)) {
                parameter.addRule(Rule.min((Integer) value));
            }
            if ("regex".equals(key)) {
                parameter.addRule(Rule.regex((String) value));
            }
            if ("prohibited".equals(key) &&
                    "true".equals(value.toString())) {
                parameter.addRule(Rule.prohibited());
            }
            if ("email".equals(key) &&
                    "true".equals(value.toString())) {
                parameter.addRule(Rule.email());
            }
            if ("integer".equals(key) &&
                    "true".equals(value.toString())) {
                parameter.addRule(Rule.integer());
            }
            if ("bool".equals(key) &&
                    "true".equals(value.toString())) {
                parameter.addRule(Rule.bool());
            }
            if ("list".equals(key) &&
                    "true".equals(value.toString())) {
                parameter.addRule(Rule.list());
            }
            if ("map".equals(key) &&
                    "true".equals(value.toString())) {
                parameter.addRule(Rule.map());
            }
            if ("equals".equals(key)) {
                parameter.addRule(Rule.equals(value.toString()));
            }
            if ("optional".equals(key)) {
                if (value instanceof Collection) {
                    parameter.addRule(Rule.optional(ImmutableList.copyOf((Collection) value)));
                } else {
                    Iterable<String> iterable = Splitter.on(",").trimResults().omitEmptyStrings().split(value.toString());
                    parameter.addRule(Rule.optional(ImmutableList.copyOf(iterable)));
                }
            }
        });
    }

    private List<Endpoint> createEndpoints(JsonArray endpoints) {
        List<Endpoint> httpEndpoints = new ArrayList<>(endpoints.size());
        for (int i = 0; i < endpoints.size(); i++) {
            HttpEndpointBuilder builder = HttpEndpoint.builder();

            JsonObject endpoint = endpoints.getJsonObject(i);
            String name = endpoint.getString("name");
            Preconditions.checkNotNull(name, "arg name cannot be null");
            builder.setName(name);

            String service = endpoint.getString("service");
            Preconditions.checkNotNull(name, "arg service cannot be null");
            builder.setService(service);
            builder.setMethod(httpMethod(endpoint));
            String path = endpoint.getString("path");
            Preconditions.checkNotNull(name, "arg path cannot be null");
            builder.setPath(path);
            httpEndpoints.add(builder.build());
        }
        return httpEndpoints;
    }
}
