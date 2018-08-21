package com.github.edgar615.gateway.core.apidiscovery;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/7/13.
 *
 * @author Edgar  Date 2017/7/13
 */
@DataObject(generateConverter = true)
public class ApiDiscoveryOptions {
    public static final String DEFAULT_PUBLISHED_ADDRESS =
            "__com.github.edgar615.gateway.api.published";

    public static final String DEFAULT_UNPUBLISHED_ADDRESS =
            "__com.github.edgar615.gateway.api.unpublished";

    private String name;

    private String publishedAddress = DEFAULT_PUBLISHED_ADDRESS;

    private String unpublishedAddress = DEFAULT_UNPUBLISHED_ADDRESS;

    public ApiDiscoveryOptions() {
    }

    public ApiDiscoveryOptions(JsonObject json) {
        this();
        ApiDiscoveryOptionsConverter.fromJson(json, this);
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        ApiDiscoveryOptionsConverter.toJson(this, json);
        return json;
    }

    public String getName() {
        return name;
    }

    public ApiDiscoveryOptions setName(String name) {
        this.name = name;
        return this;
    }

    public String getPublishedAddress() {
        return publishedAddress;
    }

    public ApiDiscoveryOptions setPublishedAddress(String publishedAddress) {
        this.publishedAddress = publishedAddress;
        return this;
    }

    public String getUnpublishedAddress() {
        return unpublishedAddress;
    }

    public ApiDiscoveryOptions setUnpublishedAddress(String unpublishedAddress) {
        this.unpublishedAddress = unpublishedAddress;
        return this;
    }
}
