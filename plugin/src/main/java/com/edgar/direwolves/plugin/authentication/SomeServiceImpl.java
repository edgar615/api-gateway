package com.edgar.direwolves.plugin.authentication;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 16-11-30.
 */
public class SomeServiceImpl implements SomeService {
  Vertx vertx;
  JsonObject config;

  public SomeServiceImpl(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    this.config = config;
  }

  @Override
  public SomeService process(String id, Handler<AsyncResult<JsonObject>> resultHandler) {
    return null;
  }
}
