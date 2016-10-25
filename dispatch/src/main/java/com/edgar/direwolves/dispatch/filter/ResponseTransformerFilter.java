package com.edgar.direwolves.dispatch.filter;

import com.edgar.direwolves.dispatch.ApiContext;
import com.edgar.direwolves.plugin.transformer.RequestTransformerPlugin;
import com.edgar.direwolves.plugin.transformer.ResponseTransformer;
import com.edgar.direwolves.plugin.transformer.ResponseTransformerPlugin;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * response_transfomer.
 * <p>
 * </pre>
 * <p>
 * Created by edgar on 16-9-20.
 */
public class ResponseTransformerFilter implements Filter {

  private static final String NAME = "response_transfomer";

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
  public void config(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    if (apiContext.apiDefinition() == null) {
      return false;
    }
    return apiContext.apiDefinition().plugin(RequestTransformerPlugin.NAME) != null;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {

    for (int i = 0; i < apiContext.response().size(); i++) {
      JsonObject response = apiContext.response().getJsonObject(i);
      transformer(apiContext, response);
    }
    completeFuture.complete(apiContext);
  }


  private void transformer(ApiContext apiContext, JsonObject response) {
    String name = response.getString("name");
    ResponseTransformerPlugin plugin =
            (ResponseTransformerPlugin) apiContext.apiDefinition()
                    .plugin(ResponseTransformerPlugin.NAME);

    ResponseTransformer transformer = plugin.transformer(name);
    if (transformer != null) {
      tranformerHeaders(response.getJsonObject("headers", new JsonObject()), transformer);
      tranformerBody(response.getJsonObject("body", new JsonObject()), transformer);
    }
  }


  private void tranformerHeaders(JsonObject headers,
                                 ResponseTransformer transformer) {
    transformer.headerRemoved().forEach(h -> headers.remove(h));
    transformer.headerReplaced().forEach(entry -> headers.put(entry.getKey(), entry.getValue()));
    transformer.headerAdded().forEach(entry -> headers.put(entry.getKey(), entry.getValue()));
  }

  private void tranformerBody(final JsonObject body,
                              ResponseTransformer transformer) {
    transformer.bodyRemoved().forEach(b -> body.remove(b));
    transformer.bodyReplaced().forEach(entry -> body.put(entry.getKey(), entry.getValue()));
    transformer.bodyAdded().forEach(entry -> body.put(entry.getKey(), entry.getValue()));
  }

}