package com.edgar.direwolves.dispatch.handler;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.dispatch.Result;
import com.edgar.direwolves.core.rpc.HttpRpcRequest;
import com.edgar.direwolves.core.rpc.RpcRequest;
import com.edgar.direwolves.dispatch.Utils;
import com.edgar.util.vertx.task.Task;
import com.google.common.collect.ImmutableList;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by edgar on 16-9-12.
 */
public class DispatchHandler implements Handler<RoutingContext> {

  private final List<Filter> filters;

  public DispatchHandler(List<Filter> filters) {
    this.filters = ImmutableList.copyOf(filters);
  }


  @Override
  public void handle(RoutingContext rc) {

    //创建上下文
    Task<ApiContext> task = apiContextTask(rc);
    task = doFilter(task, f -> Filter.PRE.equalsIgnoreCase(f.type()));

//    task.andThen("RPC")

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

//  private Task<ApiContext> rpc(RoutingContext rc, ApiContext apiContext) {
//    Task<ApiContext> task = Task.create();
//    List<RpcRequest> requests = apiContext.requests();
//    List<Future<JsonObject>> reusltsFuture = new ArrayList<>(requests.size());
//    for (int i = 0; i < requests.size(); i++) {
//      RpcRequest req = requests.get(i);
//      String type = req.type();
//      if ("http".equalsIgnoreCase(type)) {
//        Future<JsonObject> future = Future.future();
//        reusltsFuture.add(future);
//        rc.vertx().eventBus().<JsonObject>send("direwolves.rpc.http.req",
//            requests.getJsonObject(0), ar -> {
//              if (ar.succeeded()) {
//                future.complete(ar.result().body());
//              } else {
//                future.fail(ar.cause());
//              }
//            });
//      }
//    }
//    Task.par(reusltsFuture).andThen(results -> {
//      for (JsonObject result : results) {
//        apiContext.addResult(result.copy());
//      }
//      task.complete(apiContext);
//    }).onFailure(throwable -> task.fail(throwable));
//    return task;
//  }

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
