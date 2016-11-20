package com.edgar.direwolves.plugin.transformer;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * 将endpoint转换为json对象.
 * params和header的json均为{"k1", ["v1"]}，{"k1", ["v1", "v2]}格式的json对象.
 * <p/>
 * <pre>
 *   {
 * "id" : "5bbbe06b-df08-4728-b5e2-166faf912621",
 * "type" : "http",
 * "path" : "/devices",
 * "method" : "POST",
 * "params" : {
 * "q3" : [ "v3" ]
 * },
 * "headers" : {
 * "h3" : [ "v3", "v3.2" ]
 * },
 * "body" : {
 * "foo" : "bar"
 * },
 * "host" : "localhost",
 * "port" : 8080
 * }
 * </pre>
 * <p/>
 * Created by edgar on 16-9-20.
 */
public class RequestTransformerFilter implements Filter {

  private Vertx vertx;

  @Override
  public String type() {
    return PRE;
  }

  @Override
  public int order() {
    return 9500;
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
    return apiContext.apiDefinition()
        .plugin(RequestTransformerPlugin.class.getSimpleName()) != null
        && apiContext.requests().size() > 0;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    for (int i = 0; i < apiContext.requests().size(); i++) {
      JsonObject request = apiContext.requests().getJsonObject(i);
      transformer(apiContext, request);
    }
    completeFuture.complete(apiContext);
  }


  private void transformer(ApiContext apiContext, JsonObject request) {
    String name = request.getString("name");
    RequestTransformerPlugin plugin =
        (RequestTransformerPlugin) apiContext.apiDefinition()
            .plugin(RequestTransformerPlugin.class.getSimpleName());
    if (plugin == null) {
      return;
    }

    RequestTransformer transformer = plugin.transformer(name);
    if (transformer != null) {
      tranformerParams(request.getJsonObject("params"), transformer);
      tranformerHeaders(request.getJsonObject("headers"), transformer);
      if (request.containsKey("body")) {
        tranformerBody(request.getJsonObject("body"), transformer);
      }
    }
  }

  private void tranformerParams(JsonObject params,
                                RequestTransformer transformer) {
    transformer.paramRemoved().forEach(p -> params.remove(p));
    transformer.paramReplaced().forEach(entry -> params.put(entry.getKey(), entry.getValue()));
    transformer.paramAdded().forEach(entry -> params.put(entry.getKey(), entry.getValue()));
  }

  private void tranformerHeaders(JsonObject headers,
                                 RequestTransformer transformer) {
    transformer.headerRemoved().forEach(h -> headers.remove(h));
    transformer.headerReplaced().forEach(entry -> headers.put(entry.getKey(), entry.getValue()));
    transformer.headerAdded().forEach(entry -> headers.put(entry.getKey(), entry.getValue()));
  }

  private void tranformerBody(final JsonObject body,
                              RequestTransformer transformer) {
    if (body != null) {
      transformer.bodyRemoved().forEach(b -> body.remove(b));
    }
    //replace
    if (body != null) {
      transformer.bodyReplaced().forEach(entry -> body.put(entry.getKey(), entry.getValue()));
    }

    //add
    if (body != null) {
      transformer.bodyAdded().forEach(entry -> body.put(entry.getKey(), entry.getValue()));
    }
  }

}