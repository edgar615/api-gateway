package com.github.edgar615.gateway.filter;

import com.google.common.collect.Multimap;

import com.github.edgar615.gateway.core.definition.Endpoint;
import com.github.edgar615.gateway.core.definition.EventbusEndpoint;
import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.gateway.core.dispatch.Filter;
import com.github.edgar615.gateway.core.rpc.RpcRequest;
import com.github.edgar615.gateway.core.rpc.eventbus.EventbusRpcRequest;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

/**
 * EventBus类型的endpoint需要经过这个Filter转换为RpcRequest.
 * <p>
 * 请求头中的header将被忽略，如果是GET或DELETE请求，会将message设置为一个空JSON {}．
 * 然后再通过request.transformer插件将请求参数添加到message中
 *
 * @author Edgar  Date 2016/11/18
 */
public class EventBusRequestFilter implements Filter {

    EventBusRequestFilter() {
    }

    @Override
    public String type() {
        return PRE;
    }

    @Override
    public int order() {
        return 13000;
    }

    @Override
    public boolean shouldFilter(ApiContext apiContext) {
        return apiContext.apiDefinition().endpoints().stream()
                .anyMatch(e -> e instanceof EventbusEndpoint);
    }

    @Override
    public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
        apiContext.apiDefinition().endpoints().stream()
                .filter(e -> e instanceof EventbusEndpoint)
                .map(e -> toRpc(apiContext, e))
                .forEach(req -> apiContext.addRequest(req));
        completeFuture.complete(apiContext);
    }


    private RpcRequest toRpc(ApiContext apiContext, Endpoint endpoint) {
        if (endpoint instanceof EventbusEndpoint) {
            EventbusEndpoint eventbusEndpoint = (EventbusEndpoint) endpoint;
            String id = apiContext.nextRpcId();
            String name = eventbusEndpoint.name();
            String address = eventbusEndpoint.address();
            String policy = eventbusEndpoint.policy();
            Multimap header = eventbusEndpoint.headers();
            JsonObject message = apiContext.body();
            if (message == null) {
                message = new JsonObject();
            }
            return EventbusRpcRequest.create(id, name, address, policy, header, message);

        }
        return null;
    }
}
