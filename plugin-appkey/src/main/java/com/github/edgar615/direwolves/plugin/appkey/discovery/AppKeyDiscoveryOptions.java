package com.github.edgar615.direwolves.plugin.appkey.discovery;

import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 17-7-25.
 */
public class AppKeyDiscoveryOptions {
  public static final String DEFAULT_PUBLISHED_ADDRESS = "direwolves.appkey.published";

  public static final String DEFAULT_UNPUBLISHED_ADDRESS = "direwolves.appkey.unpublished";

  private String name;

  private String publishedAddress = DEFAULT_PUBLISHED_ADDRESS;

  private String unpublishedAddress = DEFAULT_UNPUBLISHED_ADDRESS;

  public AppKeyDiscoveryOptions() {}

  public AppKeyDiscoveryOptions(JsonObject json) {
    this();
    AppKeyDiscoveryOptionsConverter.fromJson(json, this);
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    AppKeyDiscoveryOptionsConverter.toJson(this, json);
    return json;
  }


  public String getName() {
    return name;
  }

  public AppKeyDiscoveryOptions setName(String name) {
    this.name = name;
    return this;
  }

  public String getPublishedAddress() {
    return publishedAddress;
  }

  public AppKeyDiscoveryOptions setPublishedAddress(String publishedAddress) {
    this.publishedAddress = publishedAddress;
    return this;
  }

  public String getUnpublishedAddress() {
    return unpublishedAddress;
  }

  public AppKeyDiscoveryOptions setUnpublishedAddress(String unpublishedAddress) {
    this.unpublishedAddress = unpublishedAddress;
    return this;
  }
}
