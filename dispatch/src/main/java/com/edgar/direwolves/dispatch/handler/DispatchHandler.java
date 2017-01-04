package com.edgar.direwolves.dispatch.handler;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.ApiProvider;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.dispatch.FilterFactory;
import com.edgar.direwolves.core.dispatch.Result;
import com.edgar.direwolves.core.rpc.FailedRpcHandler;
import com.edgar.direwolves.core.rpc.RpcHandler;
import com.edgar.direwolves.core.rpc.RpcHandlerFactory;
import com.edgar.direwolves.core.rpc.RpcResponse;
import com.edgar.direwolves.core.utils.Filters;
import com.edgar.direwolves.dispatch.Utils;
import com.edgar.util.vertx.task.Task;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.serviceproxy.ProxyHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by edgar on 16-9-12.
 */
public class DispatchHandler implements Handler<RoutingContext> {
  private static final Logger LOGGER = LoggerFactory.getLogger(DispatchHandler.class);

  private final List<Filter> filters;

  private final Map<String, RpcHandler> handlers = new ConcurrentHashMap();

  private final RpcHandler failedRpcHandler = new FailedRpcHandler("Undefined Rpc");

  private final ApiProvider apiProvider;

  private DispatchHandler(Vertx vertx, JsonObject config) {
    Lists.newArrayList(ServiceLoader.load(RpcHandlerFactory.class))
            .stream().map(f -> f.create(vertx, config))
            .forEach(h -> handlers.put(h.type().toUpperCase(), h));
    List<Filter> filterList = Lists.newArrayList(ServiceLoader.load(FilterFactory.class))
            .stream().map(f -> f.create(vertx, config))
            .collect(Collectors.toList());
    Collections.sort(filterList, (o1, o2) -> o1.order() - o2.order());
    this.filters = ImmutableList.copyOf(filterList);
    this.filters.forEach(filter -> {
      LOGGER.info("filter loaded,name->{}, type->{}, order->{}", filter.getClass().getSimpleName(),
                  filter.type(), filter.order());
    });

    String address = config.getString("api.provider.address", "direwolves.api");
    this.apiProvider = ProxyHelper.createProxy(ApiProvider.class, vertx, address);
  }

  public static DispatchHandler create(Vertx vertx, JsonObject config) {
    return new DispatchHandler(vertx, config);
  }

  @Override
  public void handle(RoutingContext rc) {

    //创建上下文
    Task<ApiContext> task = apiContextTask(rc);
    task = doFilter(task, f -> Filter.PRE.equalsIgnoreCase(f.type()));
    task = task.flatMap("RPC", apiContext -> rpc(apiContext))
            .andThen(apiContext -> apiContext.addAction("RPC", apiContext));
    task = doFilter(task, f -> Filter.POST.equalsIgnoreCase(f.type()));
    task.andThen("Response", apiContext -> {
      rc.response().putHeader("x-request-id", apiContext.id());
      Result result = apiContext.result();
      int statusCode = result.statusCode();
      boolean isArray = result.isArray();
      if (isArray) {
        rc.response()
                .setStatusCode(statusCode)
                .setChunked(true)
                .end(result.responseArray().encode());
      } else {
        rc.response()
                .setStatusCode(statusCode)
                .setChunked(true)
                .end(result.responseObject().encode());
      }
    })
            .onFailure(throwable -> FailureHandler.doHandle(rc, throwable));
  }

  public Task<ApiContext> apiContextTask(RoutingContext rc) {
    Future<ApiDefinition>
            apiDefinitionFuture = Future.future();
    getApiDefintion(rc, apiDefinitionFuture);
    return Task.create("Find api", apiDefinitionFuture)
            .map(apiDefinition -> {
              ApiContext apiContext = Utils.apiContext(rc);
              apiContext.setApiDefinition(apiDefinition);
              //设置变量
              return matches(apiContext, apiDefinition);
            });
  }

  public Task<ApiContext> doFilter(Task<ApiContext> task, Predicate<Filter> filterPredicate) {
    List<Filter> postFilters = filters.stream()
            .filter(filterPredicate)
            .collect(Collectors.toList());
    return Filters.doFilter(task, postFilters);
  }

  private ApiContext matches(ApiContext apiContext, ApiDefinition definition) {
    Multimap<String, String> params = ArrayListMultimap.create(apiContext.params());
    Pattern pattern = definition.pattern();
    String path = apiContext.path();
    Matcher matcher = pattern.matcher(path);
    if (matcher.matches()) {
      try {
        for (int i = 0; i < matcher.groupCount(); i++) {
          String group = matcher.group(i + 1);
          if (group != null) {
            final String k = "param" + i;
            final String value = URLDecoder.decode(group, "UTF-8");
            params.put(k, value);
          }
        }
      } catch (UnsupportedEncodingException e) {
        //TODO 异常处理
      }
    }
    ApiContext newApiContext =
            ApiContext.create(apiContext.method(), apiContext.path(),
                              apiContext.headers(), params, apiContext.body());
    ApiContext.copyProperites(apiContext, newApiContext);
    return newApiContext;
  }

  private Future<ApiContext> rpc(ApiContext apiContext) {
    Future<ApiContext> future = Future.future();
    List<Future<RpcResponse>> futures = apiContext.requests()
            .stream().map(req -> handlers.getOrDefault(req.type().toUpperCase(), failedRpcHandler)
                    .handle(req))
            .collect(Collectors.toList());
    Task.par(futures).andThen(responses -> {
      for (RpcResponse response : responses) {
        apiContext.addResponse(response);
      }
      future.complete(apiContext);
    }).onFailure(throwable -> future.fail(throwable));
    return future;
  }

  private void getApiDefintion(RoutingContext rc, Future<ApiDefinition> completeFuture) {
    apiProvider.match(rc.request().method().name(), rc.normalisedPath(), ar -> {
      if (ar.succeeded()) {
        try {
          completeFuture.complete(ApiDefinition.fromJson(ar.result()));
        } catch (Exception e) {
          completeFuture.fail(e);
        }
      } else {
        completeFuture.fail(ar.cause());
      }
    });

  }


}
