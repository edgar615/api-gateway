package com.github.edgar615.gateway.core.dispatch;

import io.vertx.core.Future;

public class MockFilter implements Filter {

    public MockFilter() {
    }

    @Override
    public String type() {
        return PRE;
    }

    @Override
    public int order() {
        return 0;
    }

    @Override
    public boolean shouldFilter(ApiContext apiContext) {
        return true;
    }

    @Override
    public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {

        if (!apiContext.variables().containsKey("test")) {
            throw new NullPointerException();
        }
        Boolean test = (Boolean) apiContext.variables().get("test");
        if (test) {
            completeFuture.complete(apiContext);
        } else {
            completeFuture.fail("unkown");
        }
    }

}
