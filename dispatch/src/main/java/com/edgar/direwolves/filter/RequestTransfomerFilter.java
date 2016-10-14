package com.edgar.direwolves.filter;

import com.edgar.direwolves.definition.HttpEndpoint;
import com.edgar.direwolves.dispatch.ApiContext;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * 转换为rpc调用的过滤器.
 * <p>
 * </pre>
 * <p>
 * Created by edgar on 16-9-20.
 */
public class RequestTransfomerFilter implements Filter {

  private static final String TYPE = "req_transfomer";

  private Vertx vertx;

  @Override
  public String type() {
    return TYPE;
  }

  @Override
  public void config(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    if (apiContext.apiDefinition() == null) {
      return false;
    }
    List<String> filters = apiContext.apiDefinition().filters();
    return filters.contains(TYPE);
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    apiContext.apiDefinition().endpoints().forEach(endpoint -> {
      if (endpoint instanceof HttpEndpoint) {
        HttpEndpoint httpEndpoint = (HttpEndpoint) endpoint;
        String newPath = Uitls.replaceUrl(httpEndpoint.path(), apiContext);
        JsonObject request = new JsonObject();
        apiContext.addRequest(request);
        request.put("type", "http");
        request.put("path", newPath);
        String method = httpEndpoint.method().name();
        request.put("method", method);

        if ("PUT".equalsIgnoreCase(method)
            ||  "POST".equalsIgnoreCase(method)) {
          JsonObject body = apiContext.body().copy();
         request.put("body", body);
          httpEndpoint.reqBodyArgsRemove().forEach(key -> body.remove(key));
          httpEndpoint.reqBodyArgsReplace().forEach(entry ->
                                                            body.mergeIn(
                                                                    Uitls.transformer(entry.getKey(),
                                                                                      entry.getValue(),
                                                                                      apiContext)));
          httpEndpoint.reqBodyArgsAdd().forEach(entry ->
                                                        body.mergeIn(
                                                                Uitls.transformer(entry.getKey(),
                                                                                  entry.getValue(),
                                                                                  apiContext)));
        }

        JsonObject params = Uitls.mutliMapToJson(apiContext.params());
        request.put("params", params);
        JsonObject headers = Uitls.mutliMapToJson(apiContext.headers());
        request.put("headers", headers);
        //delete
        httpEndpoint.reqHeadersRemove().forEach(key -> headers.remove(key));
        httpEndpoint.reqUrlArgsRemove().forEach(key -> params.remove(key));

        //replace
        httpEndpoint.reqHeadersReplace().forEach(entry ->
                                                         headers.mergeIn(
                                                                 Uitls.transformer(entry.getKey(),
                                                                                   entry.getValue(),
                                                                                   apiContext)));
        httpEndpoint.reqUrlArgsReplace().forEach(entry ->
                                                         params.mergeIn(
                                                                 Uitls.transformer(entry.getKey(),
                                                                                   entry.getValue(),
                                                                                   apiContext)));

        //add
        httpEndpoint.reqHeadersAdd().forEach(entry ->
                                                         headers.mergeIn(
                                                                 Uitls.transformer(entry.getKey(),
                                                                                   entry.getValue(),
                                                                                   apiContext)));
        httpEndpoint.reqUrlArgsAdd().forEach(entry ->
                                                         params.mergeIn(
                                                                 Uitls.transformer(entry.getKey(),
                                                                                   entry.getValue(),
                                                                                   apiContext)));
      }
    });
    completeFuture.complete(apiContext);
  }

}
