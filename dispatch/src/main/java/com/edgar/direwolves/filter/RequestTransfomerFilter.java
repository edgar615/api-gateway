package com.edgar.direwolves.filter;

import com.edgar.direwolves.definition.HttpEndpoint;
import com.edgar.direwolves.dispatch.ApiContext;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 转换为rpc调用的过滤器.
 * <p/>
 * </pre>
 * <p/>
 * Created by edgar on 16-9-20.
 */
public class RequestTransfomerFilter implements Filter {

    private static final String TYPE = "req_transfomer";

    private Vertx vertx;

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void config(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
    }

    @Override
    public boolean shouldFilter(ApiContext apiContext) {
        if (apiContext.apiDefinition() == null) {
            return false;
        }
        List<String> filters = apiContext.apiDefinition().filters();
        return filters.contains(TYPE);
    }

    @Override
    public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
        List<Future> futures = new ArrayList<>();
        apiContext.apiDefinition().endpoints().forEach(endpoint -> {
            if (endpoint instanceof HttpEndpoint) {
                HttpEndpoint httpEndpoint = (HttpEndpoint) endpoint;
                String newPath = Uitls.replaceUrl(httpEndpoint.path(), apiContext);
                JsonObject request = new JsonObject();
                apiContext.addRequest(request);
                request.put("id", UUID.randomUUID().toString());
                request.put("type", "http");
                request.put("path", newPath);
                String method = httpEndpoint.method().name();
                request.put("method", method);

                //服务发现
                Future<Record> serviceDiscoveryFuture = Future.future();
                futures.add(serviceDiscoveryFuture);
                vertx.eventBus().<JsonObject>send("service.discovery.select", httpEndpoint.service(), ar -> {
                    if (ar.succeeded()) {
                        JsonObject serviceJson = ar.result().body();
                        Record record = new Record(serviceJson);
                        request.put("host", record.getLocation().getString("host"));
                        request.put("port", record.getLocation().getInteger("port"));
                        serviceDiscoveryFuture.complete(record);
                    } else {
                        serviceDiscoveryFuture.fail(ar.cause());
                    }
                });

                if (apiContext.body() != null) {
                    JsonObject body = apiContext.body().copy();
                    request.put("body", body);
                    httpEndpoint.reqBodyArgsRemove().forEach(key -> body.remove(key));
                    httpEndpoint.reqBodyArgsReplace().forEach(entry ->
                            body.mergeIn(
                                    Uitls.transformer(entry.getKey(),
                                            entry.getValue(),
                                            apiContext)));
                    httpEndpoint.reqBodyArgsAdd().forEach(entry ->
                            body.mergeIn(
                                    Uitls.transformer(entry.getKey(),
                                            entry.getValue(),
                                            apiContext)));
                }

                JsonObject params = Uitls.mutliMapToJson(apiContext.params());
                request.put("params", params);
                JsonObject headers = Uitls.mutliMapToJson(apiContext.headers());
                request.put("headers", headers);
                //delete
                httpEndpoint.reqHeadersRemove().forEach(key -> headers.remove(key));
                httpEndpoint.reqUrlArgsRemove().forEach(key -> params.remove(key));

                //replace
                httpEndpoint.reqHeadersReplace().forEach(entry ->
                        headers.mergeIn(
                                Uitls.transformer(entry.getKey(),
                                        entry.getValue(),
                                        apiContext)));
                httpEndpoint.reqUrlArgsReplace().forEach(entry ->
                        params.mergeIn(
                                Uitls.transformer(entry.getKey(),
                                        entry.getValue(),
                                        apiContext)));

                //add
                httpEndpoint.reqHeadersAdd().forEach(entry ->
                        headers.mergeIn(
                                Uitls.transformer(entry.getKey(),
                                        entry.getValue(),
                                        apiContext)));
                httpEndpoint.reqUrlArgsAdd().forEach(entry ->
                        params.mergeIn(
                                Uitls.transformer(entry.getKey(),
                                        entry.getValue(),
                                        apiContext)));
            }
        });
        CompositeFuture.all(futures).setHandler(ar -> {
            if (ar.succeeded()) {
                completeFuture.complete(apiContext);
            } else {
                completeFuture.fail(ar.cause());
            }
        });
    }

}
