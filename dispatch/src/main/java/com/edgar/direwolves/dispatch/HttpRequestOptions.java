package com.edgar.direwolves.dispatch;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 单个HTTP请求的构建类.
 */
public class HttpRequestOptions {

    private final Multimap<String, String> header = ArrayListMultimap.create();
    private final Map<String, Object> params = new HashMap<>();
    private String id = UUID.randomUUID().toString();
    private String name;

    private int port = 80;

    private String host = "localhost";

    private String path = "/";

    private HttpMethod httpMethod = HttpMethod.GET;

    private JsonObject body;

    private int timeout = 10000;

    public HttpRequestOptions() {
    }

    public String getName() {
        return name;
    }

    public HttpRequestOptions setName(String name) {
        Preconditions.checkNotNull(name);
        this.name = name;
        return this;
    }

    public int getPort() {
        return port;
    }

    public HttpRequestOptions setPort(int port) {
        this.port = port;
        return this;
    }

    public String getHost() {
        return host;
    }

    public HttpRequestOptions setHost(String host) {
        Preconditions.checkNotNull(host);
        this.host = host;
        return this;
    }

    public String getPath() {
        return path;
    }

    public HttpRequestOptions setPath(String path) {
        Preconditions.checkNotNull(path);
        this.path = path;
        return this;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public HttpRequestOptions setHttpMethod(HttpMethod httpMethod) {
        Preconditions.checkNotNull(httpMethod);
        this.httpMethod = httpMethod;
        return this;
    }

    public JsonObject getBody() {
        return body;
    }

    public HttpRequestOptions setBody(JsonObject body) {
        this.body = body;
        return this;
    }

    public int getTimeout() {
        return timeout;
    }

    public HttpRequestOptions setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public HttpRequestOptions addParam(String name, Object value) {
        this.params.put(name, value);
        return this;
    }

    public Multimap<String, String> getHeader() {
        return header;
    }

    public HttpRequestOptions addHeader(String name, String value) {
        this.header.put(name, value);
        return this;
    }

    public String getId() {
        return id;
    }

    public HttpRequestOptions setId(String id) {
        this.id = id;
        return this;
    }
}