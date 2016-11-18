package com.edgar.direwolves.plugin.request;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class RequestReplaceFilter implements Filter {

  private Vertx vertx;

  @Override
  public String type() {
    return PRE;
  }

  @Override
  public int order() {
    return 10000;
  }

  @Override
  public void config(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    return true;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    for (int i = 0; i < apiContext.requests().size(); i++) {
      JsonObject request = apiContext.requests().getJsonObject(i);
      replace(apiContext, request);
    }
    completeFuture.complete(apiContext);
  }

  private void replace(ApiContext apiContext, JsonObject request) {
    JsonObject newParams = new JsonObject();
    JsonObject params = request.getJsonObject("params", new JsonObject());
    for (String key : params.fieldNames()) {
      Object newVal = apiContext.getValueByKeyword(params.getValue(key));
      if (newVal != null) {
        newParams.put(key, newVal);
      }
    }
    request.put("params", newParams);

    JsonObject newHeaders = new JsonObject();
    JsonObject headers = request.getJsonObject("headers", new JsonObject());
    for (String key : headers.fieldNames()) {
      Object newVal = apiContext.getValueByKeyword(headers.getValue(key));
      if (newVal != null) {
        newHeaders.put(key, newVal);
      }
    }
    request.put("headers", newHeaders);
    if (request.containsKey("body")) {
      JsonObject newBody = new JsonObject();
      JsonObject body = request.getJsonObject("body");
      for (String key : body.fieldNames()) {
        Object newVal = apiContext.getValueByKeyword(body.getValue(key));
        if (newVal != null) {
          newBody.put(key, newVal);
        }
      }
      request.put("body", newBody);
    }

  }


}