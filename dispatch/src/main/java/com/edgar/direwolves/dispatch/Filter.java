package com.edgar.direwolves.dispatch;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 16-9-18.
 */
public interface Filter {

    String type();

    void config(JsonObject config);

    boolean shouldFilter(ApiContext apiContext);

    void doFilter(ApiContext apiContext, Future<ApiContext> nextFuture);
}
