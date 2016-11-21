package com.edgar.direwolves.filter;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 16-11-5.
 */
public class ExtractResultFilter implements Filter {

  //extractValueFromSingleKeyModel
  @Override
  public String type() {
    return POST;
  }

  @Override
  public int order() {
    return 0;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    return true;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    JsonArray results = apiContext.results();
    JsonObject result = new JsonObject();
    if (results.size() == 1) {
      result.mergeIn(extractResult(results.getJsonObject(0)));
    } else {
      JsonObject failedResult = extractFailedResult(results);
      if (failedResult != null) {
        result.mergeIn(failedResult);
      } else {
        result.mergeIn(zipResult(apiContext));
      }
    }

    apiContext.setResponse(result);
    completeFuture.complete(apiContext);
  }

  @Override
  public void config(Vertx vertx, JsonObject config) {

  }

  private String getName(String respId, ApiContext apiContext) {
    JsonArray request = apiContext.requests();
    for (int i = 0; i < request.size(); i++) {
      JsonObject req = request.getJsonObject(i);
      String reqId = req.getString("id");
      if (respId.equalsIgnoreCase(reqId)) {
        return req.getString("name", "UNKOWN_NAME");
      }
    }
    return "UNKOWN_NAME";
  }

  private JsonObject zipResult(ApiContext apiContext) {
    JsonArray results = apiContext.results();
    JsonObject result = new JsonObject();
    result.put("statusCode", 200);
    JsonObject body = new JsonObject();
    result.put("body", body);
    result.put("isArray", false);
    for (int i = 0; i < results.size(); i++) {
      JsonObject resp = results.getJsonObject(i);
      boolean isArray = resp.getBoolean("isArray", false);
      //根据ID，从request中取出name
      String name = getName(resp.getString("id"), apiContext);
      if (isArray) {
        body.put(name, resp.getJsonArray("responseArray"));
      } else {
        body.put(name, resp.getJsonObject("responseBody"));
      }
    }
    return result;
  }

  private JsonObject extractFailedResult(JsonArray response) {
    for (int i = 0; i < response.size(); i++) {
      JsonObject resp = response.getJsonObject(i);
      int statusCode = resp.getInteger("statusCode", 200);
      if (statusCode >= 300) {
        return extractResult(resp);
      }
    }
    return null;
  }

  private JsonObject extractResult(JsonObject response) {
    JsonObject result = new JsonObject();
    int statusCode = response.getInteger("statusCode", 200);
    result.put("statusCode", statusCode);
    boolean isArray = response.getBoolean("isArray", false);
    if (isArray) {
      result.put("body", response.getJsonArray("responseArray"));
    } else {
      result.put("body", response.getJsonObject("responseBody"));
    }
    return result.copy();
  }
}
