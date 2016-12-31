package com.edgar.direwolves.dispatch.handler;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.dispatch.FilterFactory;
import com.edgar.direwolves.core.dispatch.Result;
import com.edgar.direwolves.core.rpc.FailedRpcHandler;
import com.edgar.direwolves.core.rpc.RpcHandler;
import com.edgar.direwolves.core.rpc.RpcHandlerFactory;
import com.edgar.direwolves.core.rpc.RpcResponse;
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

  public DispatchHandler(Vertx vertx, JsonObject config) {
    Lists.newArrayList(ServiceLoader.load(RpcHandlerFactory.class))
        .stream().map(f -> f.create(vertx, config))
        .forEach(h -> handlers.put(h.type(), h));
    List<Filter> filterList = Lists.newArrayList(ServiceLoader.load(FilterFactory.class))
        .stream().map(f -> f.create(vertx, config))
        .collect(Collectors.toList());
    Collections.sort(filterList, (o1, o2) -> o1.order() - o2.order());
    this.filters = ImmutableList.copyOf(filterList);
    this.filters.forEach(filter -> {
      LOGGER.info("filter loaded,name->{}, type->{}, order->{}", filter.getClass().getSimpleName(), filter.type(), filter.order());
    });
  }

  @Override
  public void handle(RoutingContext rc) {

    //创建上下文
    Task<ApiContext> task = apiContextTask(rc);
    task = doFilter(task, f -> Filter.PRE.equalsIgnoreCase(f.type()));
    task.andThen("RPC", apiContext -> rpc(rc, apiContext))
        .andThen(apiContext -> apiContext.addAction("RPC", apiContext));
    task = doFilter(task, f -> Filter.POST.equalsIgnoreCase(f.type()));
    task.andThen("Response", apiContext -> {
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
          matches(apiContext, apiDefinition);
          return apiContext;
        });
  }

  public Task<ApiContext> doFilter(Task<ApiContext> task, Predicate<Filter> filterPredicate) {
    List<Filter> postFilters = filters.stream()
        .filter(filterPredicate)
        .collect(Collectors.toList());
    for (Filter filter : postFilters) {
      task = task.flatMap(filter.getClass().getSimpleName(), apiContext -> {
        if (filter.shouldFilter(apiContext)) {
          Future<ApiContext> completeFuture = Future.future();
          filter.doFilter(apiContext.copy(), completeFuture);
          return completeFuture;
        } else {
          return Future.succeededFuture(apiContext);
        }
      }).andThen(apiContext -> apiContext.addAction(filter.getClass().getSimpleName(), apiContext));
    }
    return task;
  }

  private void matches(ApiContext apiContext, ApiDefinition definition) {
    Pattern pattern = definition.pattern();
    String path = apiContext.path();
    Matcher matcher = pattern.matcher(path);
    if (matcher.matches()) {
      try {
        for (int i = 0; i < matcher.groupCount(); i++) {
          String group = matcher.group(i + 1);
          if (group != null) {
            final String k = "param" + (i + 1);
            final String value = URLDecoder.decode(group, "UTF-8");
            apiContext.params().put(k, value);
          }
        }
      } catch (UnsupportedEncodingException e) {
        //TODO 异常处理
      }
    }
  }

  private Task<ApiContext> rpc(RoutingContext rc, ApiContext apiContext) {
    Task<ApiContext> task = Task.create();
    List<Future<RpcResponse>> futureis = apiContext.requests()
        .stream().map(req -> handlers.getOrDefault(req.type(), failedRpcHandler).handle(req))
        .collect(Collectors.toList());
    Task.par(futureis).andThen(responses -> {
      for (RpcResponse response : responses) {
        apiContext.addResponse(response);
      }
      task.complete(apiContext);
    }).onFailure(throwable -> task.fail(throwable));
    return task;
  }

  private void getApiDefintion(RoutingContext rc, Future<ApiDefinition> completeFuture) {
    JsonObject matcher = new JsonObject()
        .put("method", rc.request().method().name())
        .put("path", rc.normalisedPath());
    rc.vertx().eventBus().<List<ApiDefinition>>send("eb.api.match", matcher, ar -> {
      if (ar.succeeded()) {
        List<ApiDefinition> apiDefinitions = ar.result().body();
        if (apiDefinitions.isEmpty()) {
          completeFuture.fail("No ApiDefintion");
        } else if (apiDefinitions.size() > 1) {
          completeFuture.fail("Duplicate ApiDefintion");
        } else {
          completeFuture.complete(apiDefinitions.get(0));
        }
      } else {
        completeFuture.fail(ar.cause());
      }
    });
  }


}
