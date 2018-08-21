package com.github.edgar615.gateway.core.dispatch;

import io.vertx.core.Future;

public class MockFilter2 implements Filter {

    @Override
    public String type() {
        return PRE;
    }

    @Override
    public int order() {
        return -100;
    }

    @Override
    public boolean shouldFilter(ApiContext apiContext) {
        return true;
    }

    @Override
    public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {

        if (!apiContext.variables().containsKey("mock2")) {
            throw new NullPointerException();
        }
        Boolean test = (Boolean) apiContext.variables().get("mock2");
        if (test) {
            completeFuture.complete(apiContext);
        } else {
            completeFuture.fail("mock2 unkown");
        }
    }

}
