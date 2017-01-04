package com.edgar.direwolves.dispatch.handler;

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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by edgar on 16-9-12.
 */
public class DispatchHandler implements Handler<RoutingContext> {
  private static final Logger LOGGER = LoggerFactory.getLogger(DispatchHandler.class);

  private final List<Filter> filters;

  private final Map<String, RpcHandler> handlers = new ConcurrentHashMap();

  private final RpcHandler failedRpcHandler = new FailedRpcHandler("Undefined Rpc");

  private DispatchHandler(Vertx vertx, JsonObject config) {
    Lists.newArrayList(ServiceLoader.load(RpcHandlerFactory.class))
        .stream().map(f -> f.create(vertx, config))
        .forEach(h -> handlers.put(h.type().toUpperCase(), h));
    List<Filter> filterList = Lists.newArrayList(ServiceLoader.load(FilterFactory.class))
        .stream().map(f -> f.create(vertx, config))
        .collect(Collectors.toList());
    Filters.sort(filterList);
    this.filters = ImmutableList.copyOf(filterList);
    this.filters.forEach(filter -> {
      LOGGER.info("filter loaded,name->{}, type->{}, order->{}", filter.getClass().getSimpleName(),
          filter.type(), filter.order());
    });
  }

  public static DispatchHandler create(Vertx vertx, JsonObject config) {
    return new DispatchHandler(vertx, config);
  }

  @Override
  public void handle(RoutingContext rc) {

    //创建上下文
    Task<ApiContext> task = Task.create();
    task.complete(Utils.apiContext(rc));
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

  public Task<ApiContext> doFilter(Task<ApiContext> task, Predicate<Filter> filterPredicate) {
    List<Filter> postFilters = filters.stream()
        .filter(filterPredicate)
        .collect(Collectors.toList());
    return Filters.doFilter(task, postFilters);
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

}
