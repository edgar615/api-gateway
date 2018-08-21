package com.github.edgar615.gateway.core.utils;

import com.google.common.collect.Lists;

import com.github.edgar615.gateway.core.definition.ApiDefinition;
import com.github.edgar615.gateway.core.definition.Endpoint;
import com.github.edgar615.gateway.core.definition.SimpleHttpEndpoint;
import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.gateway.core.dispatch.Filter;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;

/**
 * Created by Edgar on 2018/4/3.
 *
 * @author Edgar  Date 2018/4/3
 */
public class ApiDefinitionFilter implements Filter {
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
        Endpoint httpEndpoint = SimpleHttpEndpoint.http("list_device", HttpMethod.GET, "/devices",
                                                        80, "localhost");
        ApiDefinition apiDefinition = ApiDefinition.create("list_device_version2", HttpMethod.GET,
                                                           "/v2/devices",
                                                           Lists.newArrayList(httpEndpoint));
        apiContext.setApiDefinition(apiDefinition);
        completeFuture.complete(apiContext);
    }
}
