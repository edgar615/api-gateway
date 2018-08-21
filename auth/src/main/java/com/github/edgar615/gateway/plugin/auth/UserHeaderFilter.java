package com.github.edgar615.gateway.plugin.auth;

import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.gateway.core.dispatch.Filter;
import com.github.edgar615.gateway.core.rpc.RpcRequest;
import com.github.edgar615.gateway.core.rpc.eventbus.EventbusRpcRequest;
import com.github.edgar615.gateway.core.rpc.http.HttpRpcRequest;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.Base64;

/**
 * 在校验通过之后，将调用方信息base64编码后，加入到下游服务的请求头
 * x-client-appkey : base64编码的字符串
 */
public class UserHeaderFilter implements Filter {

    private final Vertx vertx;

    UserHeaderFilter(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
    }

    @Override
    public String type() {
        return PRE;
    }

    @Override
    public int order() {
        return 16100;
    }

    @Override
    public boolean shouldFilter(ApiContext apiContext) {
        return apiContext.principal() != null;
    }

    @Override
    public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
        String principalBase64 =
                Base64.getEncoder().encodeToString(apiContext.principal().encode().getBytes());
        for (RpcRequest rpcRequest : apiContext.requests()) {
            if (rpcRequest instanceof HttpRpcRequest) {
                HttpRpcRequest httpRpcRequest = (HttpRpcRequest) rpcRequest;
                httpRpcRequest.addHeader("x-client-principal", principalBase64);
            }
            if (rpcRequest instanceof EventbusRpcRequest) {
                EventbusRpcRequest eventbusRpcRequest = (EventbusRpcRequest) rpcRequest;
                eventbusRpcRequest.addHeader("x-client-principal", principalBase64);
            }
        }
        completeFuture.complete(apiContext);
    }

}