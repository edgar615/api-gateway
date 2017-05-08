package com.edgar.direwolves.dispatch.verticle;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public interface RecordSelect {


  static RecordSelect create(Vertx vertx, JsonObject config) {
    return new RecordSelectImpl(vertx, config);
  }
}
