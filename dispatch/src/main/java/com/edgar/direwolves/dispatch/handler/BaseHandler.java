package com.edgar.direwolves.dispatch.handler;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * http的基础处理类.
 * 设置响应对content-type
 *
 * @author Edgar  Date 2016/2/18
 */
public class BaseHandler implements Handler<RoutingContext> {

    public static Handler<RoutingContext> create() {
        return new BaseHandler();
    }

    @Override
    public void handle(RoutingContext rc) {
        rc.response().setChunked(true)
                .putHeader("content-type", "application/json;charset=utf-8");
        rc.next();
    }

}