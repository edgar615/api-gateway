package com.edgar.direwolves.dispatch;

import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.google.common.base.Strings;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.DecodeException;
import io.vertx.ext.web.RoutingContext;

import java.util.HashMap;
import java.util.Map;
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

//    private Map<String, String> requestVirables(HttpServerRequest req) {
//        Map<String, String> variables = new HashMap<>();
//        variables.put("scheme", req.scheme());
//        variables.put("method", req.method().name());
//        variables.put("query_string", req.query());
//        variables.put("uri", req.uri());
////        variables.put("ip", req.remoteAddress().host());
////        request.body # request body sent by the client (see below)
////        request.scheme # "http"
////        request.path_info # "/foo",
////                request.port # 80
////        request.request_method # "GET",
////                request.query_string # "",
////                request.content_length # length of request.body,
////                request.media_type # media type of request.body , content type?
////                request.host # "example.com"
////        request.url # "http://example.com/example/foo"
////        request.ip # client IP address
////        request.env # raw env hash handed in by Rack,
////                request.get? # true (similar methods for other verbs)
////        request.secure? # false (would be true over ssl)
////        request.forwarded? # true (if running behind a reverse proxy)
////        request.cookies # hash of browser cookies,
////        request.xhr? # is this an ajax request?
////                request.script_name # "/example"
////        request.form_data? # false
////        request.referrer # the referrer of the client or '/'
//    }
}
