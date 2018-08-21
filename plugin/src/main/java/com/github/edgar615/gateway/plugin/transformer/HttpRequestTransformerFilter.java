package com.github.edgar615.gateway.plugin.transformer;

import com.google.common.collect.Multimap;

import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.gateway.core.rpc.RpcRequest;
import com.github.edgar615.gateway.core.rpc.http.HttpRpcRequest;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 将RpcRequest中的请求头，请求参数，请求体按照RequestTransformerPlugin中的配置处理.
 * <p>
 * 执行的顺序为: remove replace add
 * <p>
 * 该filter的order=15000。
 * <p>
 * 接受的参数
 * <p>
 * "request.transformer": {
 * "header.add": [
 * "x-auth-userId:$user.userId",
 * "x-auth-companyCode:$user.companyCode",
 * "x-policy-owner:individual"
 * ],
 * "header.remove": [
 * "Authorization"
 * ],
 * "header.replace": [
 * "x-app-verion:x-client-version"
 * ],
 * "query.add": [
 * "userId:$user.userId"
 * ],
 * "query.remove": [
 * "appKey",
 * "nonce"
 * ],
 * "query.replace": [
 * "x-app-verion:x-client-version"
 * ],
 * "body.add": [
 * "userId:$user.userId"
 * ],
 * "body.remove": [
 * "appKey",
 * "nonce"
 * ],
 * "body.replace": [
 * "x-app-verion:x-client-version"
 * ]
 * }
 * Created by edgar on 16-9-20.
 */
public class HttpRequestTransformerFilter extends AbstractTransformerFilter {

    private final AtomicReference<RequestTransformer> reference = new AtomicReference<>();

    HttpRequestTransformerFilter(JsonObject config) {
        updateConfig(config);
    }

    @Override
    public boolean shouldFilter(ApiContext apiContext) {
        if (apiContext.apiDefinition() == null) {
            return false;
        }
        if (apiContext.requests().size() > 0
            && apiContext.requests().stream()
                    .anyMatch(e -> e instanceof HttpRpcRequest)) {
            return reference.get() != null
                   || apiContext.apiDefinition()
                              .plugin(RequestTransformerPlugin.class.getSimpleName()) != null;
        }
        return false;
    }

    @Override
    public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
        for (int i = 0; i < apiContext.requests().size(); i++) {
            RpcRequest request = apiContext.requests().get(i);
            if (request instanceof HttpRpcRequest) {
                if (reference.get() != null) {
                    doTransformer((HttpRpcRequest) request, reference.get());
                }
                transformer(apiContext, (HttpRpcRequest) request);
            }
        }
        completeFuture.complete(apiContext);
    }

    @Override
    public void updateConfig(JsonObject config) {
        super.setGlobalTransformer(config, reference);
    }

    private void transformer(ApiContext apiContext, HttpRpcRequest request) {
        String name = request.name();
        RequestTransformerPlugin plugin =
                (RequestTransformerPlugin) apiContext.apiDefinition()
                        .plugin(RequestTransformerPlugin.class.getSimpleName());
        if (plugin == null) {
            return;
        }
        RequestTransformer transformer = plugin.transformer(name);
        if (transformer != null) {
            doTransformer(request, transformer);
        }
    }

    private void doTransformer(HttpRpcRequest request, RequestTransformer transformer) {
        Multimap<String, String> params = tranformerParams(request.params(), transformer);
        request.clearParams().addParams(params);
        Multimap<String, String> headers = tranformerHeaders(request.headers(), transformer);
        request.clearHeaders().addHeaders(headers);
        if (request.body() != null) {
            JsonObject body = tranformerBody(request.body(), transformer);
            request.setBody(body);
        }
    }
}