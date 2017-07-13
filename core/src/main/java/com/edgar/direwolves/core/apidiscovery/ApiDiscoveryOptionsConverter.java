package com.edgar.direwolves.core.apidiscovery;

import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/7/13.
 *
 * @author Edgar  Date 2017/7/13
 */
class ApiDiscoveryOptionsConverter {

  static void fromJson(JsonObject json, ApiDiscoveryOptions obj) {
    if (json.getValue("publishedAddress") instanceof String) {
      obj.setPublishedAddress((String) json.getValue("publishedAddress"));
    }
    if (json.getValue("unpublishedAddress") instanceof String) {
      obj.setUnpublishedAddress((String) json.getValue("unpublishedAddress"));
    }
    if (json.getValue("name") instanceof String) {
      obj.setName((String) json.getValue("name"));
    }
  }

  static void toJson(ApiDiscoveryOptions obj, JsonObject json) {
    if (obj.getPublishedAddress() != null) {
      json.put("publishedAddress", obj.getPublishedAddress());
    }
    if (obj.getUnpublishedAddress() != null) {
      json.put("backendConfiguration", obj.getUnpublishedAddress());
    }
    if (obj.getName() != null) {
      json.put("name", obj.getName());
    }
  }
}