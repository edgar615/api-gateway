package com.edgar.direwolves.dispatch;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Multimap;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

import java.util.UUID;

public class ApiContext {

    private final String id = UUID.randomUUID().toString();

    private final String path;

    private final HttpMethod method;

    private final Multimap<String, String> headers;

    private final Multimap<String, String> params;

    private final JsonObject body;

    private final String token;

    private ApiContext(String path, HttpMethod method, Multimap<String, String> headers, Multimap<String, String> params, JsonObject body, String token) {
        this.path = path;
        this.method = method;
        this.headers = headers;
        this.params = params;
        this.body = body;
        this.token = token;
    }

    static Builder builder() {
        return new Builder();
    }

    public Multimap<String, String> getParams() {
        return params;
    }

    public Multimap<String, String> getHeaders() {
        return headers;
    }

    public JsonObject getBody() {
        return body;
    }

    public String getPath() {
        return path;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getToken() {
        return token;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("Api")
                .add("method", method)
                .add("path", path)
                .add("params", params)
                .add("headers", headers)
                .add("body", body)
                .add("token", token)
                .toString();
    }

    static class Builder {
        private String path = "/";
        private HttpMethod method = HttpMethod.GET;
        private Multimap<String, String> headers;
        private Multimap<String, String> params;
        private JsonObject body;
        private String token;

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

        public Builder setToken(String token) {
            this.token = token;
            return this;
        }

        public ApiContext build() {
            return new ApiContext(path, method, headers, params, body, token);
        }
    }
}