package com.edgar.direwolves.dispatch;

import com.edgar.direwolves.utils.MultiMapToMultimap;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.google.common.base.Strings;
import io.vertx.core.http.HttpMethod;
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
        String authorization = rc.request().getHeader("Authorization");
        if (!Strings.isNullOrEmpty(authorization)) {
            if (authorization.startsWith("Bearer ")) {
                String token = authorization.substring(7);
                builder.setToken(token);
            } else {
                throw SystemException.create(DefaultErrorCode.INVALID_TOKEN);
            }
        }
        builder.setHeaders(MultiMapToMultimap.instance().apply(rc.request().headers()));
        builder.setParams(MultiMapToMultimap.instance().apply(rc.request().params()));
        if (rc.request().method() == HttpMethod.POST || rc.request().method() == HttpMethod.PUT) {
            builder.setBody(rc.getBodyAsJson());
        }
        return builder.build();
    }
}
