package com.edgar.direwolves.dispatch;

import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.google.common.base.Strings;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.DecodeException;
import io.vertx.ext.web.RoutingContext;

import java.util.function.Function;

/**
 * 将RoutingContext转换为ApiContext.
 *
 * @author Edgar  Date 2016/9/12
 */
public class ApiContextTransformer implements Function<RoutingContext, ApiContext> {

    private static final ApiContextTransformer INSTANCE = new ApiContextTransformer();

    private ApiContextTransformer() {

    }

    public static Function<RoutingContext, ApiContext> instance() {
        return INSTANCE;
    }

    @Override
    public ApiContext apply(RoutingContext rc) {
        ApiContext.Builder builder = ApiContext.builder();
        builder.setPath(rc.normalisedPath())
                .setMethod(rc.request().method());
        builder.setHeaders(MultiMapToMultimap.instance().apply(rc.request().headers()));
        builder.setParams(MultiMapToMultimap.instance().apply(rc.request().params()));
        if (rc.request().method() == HttpMethod.POST || rc.request().method() == HttpMethod.PUT) {
            try {
                builder.setBody(rc.getBodyAsJson());
            } catch (DecodeException e) {
                throw SystemException.create(DefaultErrorCode.INVALID_JSON);
            }
        }
        return builder.build();
    }
}
