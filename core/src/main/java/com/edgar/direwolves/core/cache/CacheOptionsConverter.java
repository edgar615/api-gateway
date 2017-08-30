package com.edgar.direwolves.core.cache;

import io.vertx.core.json.JsonObject;

class CacheOptionsConverter {

  static void fromJson(JsonObject json, CacheOptions obj) {
    if (json.getValue("expireAfterWrite") instanceof Number) {
      obj.setExpireAfterWrite(((Number) json.getValue("expireAfterWrite")).longValue());
    }
  }

  static void toJson(CacheOptions obj, JsonObject json) {
    if (obj.getExpireAfterWrite() != null) {
      json.put("expireAfterWrite", obj.getExpireAfterWrite());
    }
  }
}