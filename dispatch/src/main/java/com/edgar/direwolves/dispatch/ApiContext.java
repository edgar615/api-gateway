package com.edgar.direwolves.dispatch;

import com.edgar.direwolves.definition.ApiDefinition;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ApiContext {

    private final String id = UUID.randomUUID().toString();

    private final String path;

    private final HttpMethod method;

    private final Multimap<String, String> headers;

    private final Multimap<String, String> params;

    private final JsonObject body;

    private JsonObject principal;

    private final Map<String, Object> variables = new HashMap<>();

    private ApiDefinition apiDefinition;

    private ApiContext(String path, HttpMethod method, Multimap<String, String> headers, Multimap<String, String> params, JsonObject body) {
        this.path = path;
        this.method = method;
        this.headers = headers;
        this.params = params;
        this.body = body;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Multimap<String, String> params() {
        return params;
    }

    public Multimap<String, String> headers() {
        return headers;
    }

    public JsonObject body() {
        return body;
    }

    public String path() {
        return path;
    }

    public HttpMethod method() {
        return method;
    }

    public JsonObject principal() {
        return principal;
    }

    public void setPrincipal(JsonObject principal) {
        this.principal = principal;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void addVariable(String name, Object value) {
        variables.put(name, value);
    }

    public ApiDefinition getApiDefinition() {
        return apiDefinition;
    }

    public void setApiDefinition(ApiDefinition apiDefinition) {
        this.apiDefinition = apiDefinition;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("Api")
                .add("method", method)
                .add("path", path)
                .add("params", params)
                .add("headers", headers)
                .add("body", body)
                .add("principal", principal.encode())
                .add("variables", variables)
                .toString();
    }

    public static class Builder {
        private String path = "/";
        private HttpMethod method = HttpMethod.GET;
        private Multimap<String, String> headers = ArrayListMultimap.create();
        private Multimap<String, String> params = ArrayListMultimap.create();
        private JsonObject body;

        private Builder() {
        }

        public Builder setPath(String path) {
            this.path = path;
            return this;
        }

        public Builder setMethod(HttpMethod method) {
            this.method = method;
            return this;
        }

        public Builder setHeaders(Multimap<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder setParams(Multimap<String, String> params) {
            this.params = params;
            return this;
        }

        public Builder setBody(JsonObject body) {
            this.body = body;
            return this;
        }

        public ApiContext build() {
            return new ApiContext(path, method, headers, params, body);
        }
    }
}