package com.github.edgar615.gateway.core.dispatch;

import io.vertx.core.Future;

public class MockFilter4 implements Filter {

    @Override
    public String type() {
        return PRE;
    }

    @Override
    public int order() {
        return -10;
    }

    @Override
    public boolean shouldFilter(ApiContext apiContext) {
        return false;
    }

    @Override
    public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {

        if (!apiContext.variables().containsKey("mock4")) {
            throw new NullPointerException();
        }
        Boolean test = (Boolean) apiContext.variables().get("mock4");
        if (test) {
            completeFuture.complete(apiContext);
        } else {
            completeFuture.fail("mock4 unkown");
        }
    }

}
