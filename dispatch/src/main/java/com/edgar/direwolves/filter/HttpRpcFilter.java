package com.edgar.direwolves.filter;

import com.edgar.direwolves.dispatch.ApiContext;
import com.edgar.direwolves.dispatch.Http;
import com.edgar.direwolves.dispatch.HttpResult;
import com.edgar.util.vertx.task.Task;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by edgar on 16-10-16.
 */
public class HttpRpcFilter implements Filter {
  private static final String NAME = "http-rpc";

  private Vertx vertx;

  @Override
  public String name() {
    return NAME;
  }

  @Override
  public String type() {
    return PRE;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    return apiContext.request().size() > 0;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    List<Future<HttpResult>> futures = new ArrayList<>();
    HttpClient httpClient = vertx.createHttpClient();
    for (int i = 0; i < apiContext.request().size(); i++) {
      Future<HttpResult> future = Http.request(httpClient, apiContext.request().getJsonObject(i));
      futures.add(future);
    }
    Task.par(futures)
            .andThen(results ->
                             results.forEach(result -> apiContext.addResult(result.toJson()))
            ).andThen(results -> completeFuture.complete(apiContext))
            .onFailure(throwable -> completeFuture.fail(throwable));
  }

  @Override
  public void config(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
  }
}
