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

    private String apiName;

    private JsonObject principal;

    private ApiContext(String path, HttpMethod method, Multimap<String, String> headers, Multimap<String, String> params, JsonObject body) {
        this.path = path;
        this.method = method;
        this.headers = headers;
        this.params = params;
        this.body = body;
    }

    static Builder builder() {
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

    public String apiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public JsonObject principal() {
        return principal;
    }

    public void setPrincipal(JsonObject principal) {
        this.principal = principal;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("Api")
                .add("method", method)
                .add("path", path)
                .add("params", params)
                .add("headers", headers)
                .add("body", body)
                .toString();
    }

    static class Builder {
        private String path = "/";
        private HttpMethod method = HttpMethod.GET;
        private Multimap<String, String> headers;
        private Multimap<String, String> params;
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