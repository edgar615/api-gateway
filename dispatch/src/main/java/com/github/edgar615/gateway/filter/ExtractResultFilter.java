package com.github.edgar615.gateway.filter;

import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.gateway.core.dispatch.Filter;
import com.github.edgar615.gateway.core.dispatch.Result;
import com.github.edgar615.gateway.core.rpc.RpcRequest;
import com.github.edgar615.gateway.core.rpc.RpcResponse;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * 该filter将RpcResponse返回的值聚合为一个result.
 * <p>
 * 该filter的order=-2147483638
 * <p>
 * 如果RpcResponse为空，直接抛出异常.
 * 如果只有一个RpcResponse，直接返回这个RpcResponse
 * 如果有多个RpcResponse，需要根据RpcResponse的状态来判断,
 * 如果有RpcResponse的statusCode>300，则我们认为该RpcResponse请求返回了错误，这个错误可能是网络问题，也可能是参数不符合接口定义，也可能是RPC
 * 服务的BUG导致。此时直接返回该错误.<b>未来我们可以针对这种错误在增加处理策略</b>,
 * 如果所有的RPC请求都是成功的请求，那么将所有的RpcResponse按照他们的name属性组合成一个JsonObject对象，而statusCode默认为200
 * <p>
 * 该filter的order=0
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
    return Integer.MAX_VALUE + 1000;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    return true;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    List<RpcResponse> results = apiContext.responses();
    Result result = null;
    if (results.size() == 0) {
      completeFuture.fail(SystemException.create(DefaultErrorCode.UNKOWN)
                                  .set("details", "The result of RPC was not found"));
      return;
    } else if (results.size() == 1) {
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
//    JsonObject result = new JsonObject();
    int statusCode = response.statusCode();
//    result.put("statusCode", statusCode);
    boolean isArray = response.isArray();
    if (isArray) {
      return Result.createJsonArray(statusCode, response.responseArray(), null);
    } else {
      return Result.createJsonObject(statusCode, response.responseObject(), null);
    }
  }
}
