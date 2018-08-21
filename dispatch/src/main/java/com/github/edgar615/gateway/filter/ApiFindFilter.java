package com.github.edgar615.gateway.filter;

import com.github.edgar615.gateway.core.apidiscovery.ApiDiscovery;
import com.github.edgar615.gateway.core.apidiscovery.ApiDiscoveryOptions;
import com.github.edgar615.gateway.core.definition.ApiDefinition;
import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.gateway.core.dispatch.Filter;
import com.github.edgar615.gateway.core.plugin.order.OrderPlugin;
import com.github.edgar615.gateway.core.plugin.predicate.PredicatePlugin;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 该filter根据请求从API路由注册表中读取到对应的API定义.
 * <p>
 * 该filter可以接受下列的配置参数
 * <pre>
 * </pre>
 * <p>
 * <b>该filter应该在所有的filter之前执行</b>如果未找到对应的API定义，直接返回对应的异常。
 * 该filter的order=-2147482648
 * Created by edgar on 17-1-4.
 */
public class ApiFindFilter implements Filter {

    private final Vertx vertx;

    private final ApiDiscovery discovery;

    public ApiFindFilter(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        JsonObject dicoveryConfig = config.getJsonObject("api.discovery", new JsonObject());
        this.discovery = ApiDiscovery.create(vertx,
                                             new ApiDiscoveryOptions(dicoveryConfig));
    }

    @Override
    public String type() {
        return PRE;
    }

    @Override
    public int order() {
        return Integer.MIN_VALUE + 1000;
    }

    @Override
    public boolean shouldFilter(ApiContext apiContext) {
        return apiContext.apiDefinition() == null;
    }

    @Override
    public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
        discovery.filter(apiContext.method().name(), apiContext.path(), ar -> {
            if (ar.failed()) {
                failed(completeFuture, apiContext.id(), "ApiFindFailure", ar.cause());
                return;
            }
            try {
                List<ApiDefinition> apiDefinitions = ar.result();
                ApiDefinition apiDefinition =
                        extractApi(apiContext, ApiDefinition.extractInOrder(apiDefinitions));
                completeFuture.complete(apiContext.setApiDefinition(apiDefinition));
                return;
            } catch (SystemException e) {
                failed(completeFuture, apiContext.id(), "ApiFindFailure", e);
                return;
            } catch (Exception e) {
                failed(completeFuture, apiContext.id(), "ApiFindFailure", e);
                return;
            }
        });
    }

    private boolean predicate(ApiContext context, ApiDefinition apiDefinition) {
        if (apiDefinition.plugin(PredicatePlugin.class.getSimpleName()) == null) {
            return true;
        }
        PredicatePlugin predicatePlugin =
                (PredicatePlugin) apiDefinition.plugin(PredicatePlugin.class.getSimpleName());
        return predicatePlugin.predicates().stream().allMatch(p -> p.test(context));
    }

    private int order(ApiDefinition definition) {
        OrderPlugin orderPlugin =
                (OrderPlugin) definition.plugin(OrderPlugin.class.getSimpleName());
        if (orderPlugin != null) {
            return orderPlugin.order();
        }
        return Integer.MAX_VALUE;
    }


    private ApiDefinition extractApi(ApiContext context, List<ApiDefinition> apiDefinitions) {
        List<ApiDefinition> predicateList = apiDefinitions.stream()
                .filter(d -> predicate(context, d))
                .collect(Collectors.toList());
        if (predicateList.isEmpty()) {//没有API
            throw SystemException.create(DefaultErrorCode.RESOURCE_NOT_FOUND)
                    .set("details", String.format("Api: %s:%s",
                                                  context.method().name(),
                                                  context.path()));
        }
        if (predicateList.size() == 1) {//只有一个
            return predicateList.get(0);
        }
        //有多个，用order排序
        List<ApiDefinition> sortedDefinitions =
                predicateList.stream().sorted(Comparator.comparingInt(this::order))
                        .collect(Collectors.toList());
        ApiDefinition d1 = sortedDefinitions.get(0);
        ApiDefinition d2 = sortedDefinitions.get(1);
        if (order(d1) == order(d2)) {
            throw SystemException.create(DefaultErrorCode.CONFLICT)
                    .set("details", String.format("Api: %s:%s",
                                                  context.method().name(),
                                                  context.path()));
        }
        return d1;
    }

}
