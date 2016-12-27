package com.edgar.direwolves.filter;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.dispatch.Result;
import com.edgar.direwolves.core.rpc.RpcRequest;
import com.edgar.direwolves.core.rpc.RpcResponse;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * Created by edgar on 16-11-5.
 */
public class ExtractResultFilter implements Filter {

  ExtractResultFilter() {
  }

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
    List<RpcResponse> results = apiContext.responses();
    Result result = null;
    if (results.size() == 1) {
      result = extractResult(results.get(0));
    } else {
      result = extractFailedResult(results);
      if (result == null) {
        result = zipResult(apiContext);
      }
    }

    apiContext.setResult(result);
    completeFuture.complete(apiContext);
  }

  private String getName(String respId, ApiContext apiContext) {
    List<RpcRequest> requests = apiContext.requests();
    for (int i = 0; i < requests.size(); i++) {
      RpcRequest req = requests.get(i);
      String reqId = req.id();
      if (respId.equalsIgnoreCase(reqId)) {
        return req.name();
      }
    }
    return "UNKOWN_NAME";
  }

  private Result zipResult(ApiContext apiContext) {
    List<RpcResponse> responses = apiContext.responses();
    JsonObject body = new JsonObject();
    for (int i = 0; i < responses.size(); i++) {
      RpcResponse resp = responses.get(i);
      boolean isArray = resp.isArray();
      //根据ID，从request中取出name
      String name = getName(resp.id(), apiContext);
      if (isArray) {
        body.put(name, resp.responseArray());
      } else {
        body.put(name, resp.responseObject());
      }
    }
    return Result.createJsonObject(200, body, null);
  }

  private Result extractFailedResult(List<RpcResponse> responses) {
    for (int i = 0; i < responses.size(); i++) {
      RpcResponse resp = responses.get(i);
      int statusCode = resp.statusCode();
      if (statusCode >= 300) {
        return extractResult(resp);
      }
    }
    return null;
  }

  private Result extractResult(RpcResponse response) {
    JsonObject result = new JsonObject();
    int statusCode = response.statusCode();
    result.put("statusCode", statusCode);
    boolean isArray = response.isArray();
    if (isArray) {
      return Result.createJsonArray(statusCode, response.responseArray(), null);
    } else {
      return Result.createJsonObject(statusCode, response.responseObject(), null);
    }
  }
}
