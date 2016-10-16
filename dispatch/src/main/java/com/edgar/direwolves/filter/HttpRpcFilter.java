package com.edgar.direwolves.filter;

import com.edgar.direwolves.dispatch.ApiContext;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 16-10-16.
 */
public class HttpRpcFilter implements Filter {
    private static final String TYPE = "http-rpc";

    private Vertx vertx;

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public boolean shouldFilter(ApiContext apiContext) {
        return apiContext.request().size() > 0;
    }

    @Override
    public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {

        for (int i = 0; i < apiContext.request().size(); i++) {

        }
    }

    @Override
    public void config(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
    }
}
