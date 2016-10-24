package com.edgar.direwolves.dispatch;

import com.edgar.direwolves.definition.ApiDefinition;
import com.edgar.direwolves.definition.Endpoint;
import com.edgar.direwolves.definition.HttpEndpoint;
import com.edgar.direwolves.eb.ApiMatchHandler;
import com.edgar.direwolves.filter.Filters;
import com.edgar.util.vertx.task.Task;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by edgar on 16-9-12.
 */
public class DispatchHandler implements Handler<RoutingContext> {

  private JsonObject httpJson(ApiContext apiContext, HttpEndpoint endpoint) {
    //host,port
    //path
    //query
    //body
    //header
    return null;
  }

  @Override
  public void handle(RoutingContext rc) {

    //创建上下文
    Future<ApiDefinition> apiDefinitionFuture = Future.future();
    getApiDefintion(rc, apiDefinitionFuture);
    Task.create(apiDefinitionFuture)
            .map(apiDefinition -> {
              ApiContext apiContext = ApiContext.create(rc);
              apiContext.setApiDefinition(apiDefinition);
              //设置变量
              matches(apiContext, apiDefinition);
              return apiContext;
            }).flatMapTask("do filters", apiContext -> Filters.instance().doFilter(apiContext))
            .andThen(apiContext ->{
              ApiDefinition apiDefinition = apiContext.apiDefinition();
              List<Endpoint> endpoints = apiDefinition.endpoints();
              endpoints.forEach(endpoint -> {
                if (endpoint instanceof HttpEndpoint) {
                  HttpEndpoint httpEndpoint = (HttpEndpoint) endpoint;


                }
              });
            } )
            .onFailure(throwable -> {
              rc.response().setStatusCode(404).setChunked(true)
                      .end(new JsonObject()
                                   .put("error", throwable.getMessage())
                                   .encode());
            });
//    apiDefinitionFuture.setHandler(ar -> {
//      if (ar.succeeded()) {
//        ApiDefinition apiDefinition = ar.result();
//        apiContext.setApiDefinition(apiDefinition);
//        //设置变量
//        matches(apiContext, apiDefinition);
//        Filters filters = Filters.instance();
//        Task<ApiContext> task = filters.doFilter(apiContext);
//        task.andThen(context -> rc.response()
//                .end(new JsonObject()
//                             .put("apiName", apiDefinition.name())
//                             .encode()))
//                .onFailure(throwable -> {
//                  throwable.printStackTrace();
//                  rc.response().setStatusCode(404)
//                          .end(new JsonObject()
//                                       .put("foo", "bar")
//                                       .encode());
//                });
//      } else {
//        rc.response().setStatusCode(404).setChunked(true)
//                .end(new JsonObject()
//                             .put("error", ar.cause().getMessage())
//                             .encode());
//      }
//    });
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

  private void getApiDefintion(RoutingContext rc, Future<ApiDefinition> completeFuture) {
    JsonObject matcher = new JsonObject()
            .put("method", rc.request().method().name())
            .put("path", rc.normalisedPath());
    rc.vertx().eventBus().<List<ApiDefinition>>send(ApiMatchHandler.ADDRESS, matcher, ar -> {
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
