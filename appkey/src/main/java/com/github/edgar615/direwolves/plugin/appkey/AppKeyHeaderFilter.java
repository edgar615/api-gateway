package com.github.edgar615.direwolves.plugin.appkey;

import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.direwolves.core.rpc.RpcRequest;
import com.github.edgar615.direwolves.core.rpc.eventbus.EventbusRpcRequest;
import com.github.edgar615.direwolves.core.rpc.http.HttpRpcRequest;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.Base64;

/**
 * AppKey的校验.
 * 在校验通过之后，将调用方信息base64编码后，加入到下游服务的请求头
 * x-client-appkey : base64编码的字符串
 */
public class AppKeyHeaderFilter implements Filter {

  private final Vertx vertx;

  AppKeyHeaderFilter(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
  }

  @Override
  public String type() {
    return PRE;
  }

  @Override
  public int order() {
    return 16000;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    return apiContext.apiDefinition().plugin(AppKeyPlugin.class.getSimpleName()) != null;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    JsonObject appKeyJson = new JsonObject();
    appKeyJson.put("appKey", apiContext.variables().getOrDefault("client_appKey", "anonymous"));
    if (apiContext.variables().containsKey("client_clientCode")) {
      appKeyJson.put("clientCode", apiContext.variables().getOrDefault("client_clientCode", "-1"));
    }
    if (apiContext.variables().containsKey("client_appName")) {
      appKeyJson.put("appName", apiContext.variables().getOrDefault("client_appName", "unkown"));
    }
    String clientBase64 = Base64.getEncoder().encodeToString(appKeyJson.encode().getBytes());
    for (RpcRequest rpcRequest : apiContext.requests()) {
      if (rpcRequest instanceof HttpRpcRequest) {
        HttpRpcRequest httpRpcRequest = (HttpRpcRequest) rpcRequest;
        httpRpcRequest.addHeader("x-client-appkey", clientBase64);
      }
      if (rpcRequest instanceof EventbusRpcRequest) {
        EventbusRpcRequest eventbusRpcRequest = (EventbusRpcRequest) rpcRequest;
        eventbusRpcRequest.addHeader("x-client-appkey", clientBase64);
      }
    }
    completeFuture.complete(apiContext);
  }

}