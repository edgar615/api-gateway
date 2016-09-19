package com.edgar.direwolves.dispatch;

import com.edgar.direwolves.definition.ApiDefinition;
import com.edgar.direwolves.definition.ApiDefinitionRegistry;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by edgar on 16-9-12.
 */
public class DispatchHandler implements Handler<RoutingContext> {
    @Override
    public void handle(RoutingContext rc) {

        //创建上下文
        ApiContext apiContext = ApiContextTransformer.instance().apply(rc);
        //根据api的路径匹配
        ApiDefinitionRegistry registry = ApiDefinitionRegistry.create();

        Optional<ApiDefinition> optional =
                registry.getDefinitions().stream().filter(definition -> ApiMatcher.instance().apply(apiContext, definition))
                        .findFirst();
        if (optional.isPresent()) {
            ApiDefinition apiDefinition = optional.get();
            apiContext.setApiName(apiDefinition.name());

            Future<ApiContext> future = Future.future();
            new JWTFilter().doFilter(apiContext, future);
            //TODO 路由转发
            rc.response().setChunked(true)
                    .end(new JsonObject()
                            .put("apiName", apiContext.apiName())
                            .encode());
        } else {
            //TODO 404
            rc.response().setStatusCode(404).setChunked(true)
                    .end(new JsonObject()
                            .put("foo", "bar")
                            .encode());
        }
    }


}
