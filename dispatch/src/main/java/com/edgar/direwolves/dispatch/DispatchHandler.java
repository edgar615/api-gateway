package com.edgar.direwolves.dispatch;

import com.edgar.direwolves.definition.ApiDefinition;
import com.edgar.direwolves.definition.ApiDefinitionRegistry;
import com.edgar.direwolves.filter.Filters;
import com.edgar.util.vertx.task.Task;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.Optional;

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
            apiContext.setApiDefinition(apiDefinition);
            String apiName = apiDefinition.name();
            Filters filters = new Filters();
            filters.init(rc.vertx());
            Task<ApiContext> task = filters.doFilter(apiContext);
            task.andThen(context -> rc.response().setChunked(true)
                    .end(new JsonObject()
                            .put("apiName", apiName)
                            .encode()))
                    .onFailure(throwable -> {
                        throwable.printStackTrace();
                        rc.response().setStatusCode(404).setChunked(true)
                                .end(new JsonObject()
                                        .put("foo", "bar")
                                        .encode());
                    });
        } else {
            //TODO 404
            rc.response().setStatusCode(404).setChunked(true)
                    .end(new JsonObject()
                            .put("foo", "bar")
                            .encode());
        }
    }


}
