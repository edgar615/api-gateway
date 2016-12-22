package com.edgar.direwolves.plugin.transformer;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * response_transfomer.
 * <p/>
 * </pre>
 * <p/>
 * Created by edgar on 16-9-20.
 */
public class ResponseTransformerFilter implements Filter {

  ResponseTransformerFilter() {
  }

  @Override
  public String type() {
    return POST;
  }

  @Override
  public int order() {
    return 100;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    if (apiContext.apiDefinition() == null) {
      return false;
    }
    return apiContext.apiDefinition()
        .plugin(ResponseTransformerPlugin.class.getSimpleName()) != null;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    ResponseTransformerPlugin plugin =
        (ResponseTransformerPlugin) apiContext.apiDefinition()
            .plugin(ResponseTransformerPlugin.class.getSimpleName());

    JsonObject response = apiContext.response();
    //如果body的JsonObject直接添加，如果是JsonArray，不支持body的修改
    boolean isArray = response.getBoolean("isArray");
    //body目前仅考虑JsonObject的替换
    if (!isArray) {
      tranformerBody(response.getJsonObject("body"), plugin);
    }
    JsonObject header = response.getJsonObject("headers", new JsonObject());
    response.put("headers", header);
    tranformerHeaders(header, plugin);

    //变量替换
    replace(apiContext, response);

    completeFuture.complete(apiContext);
  }

  private void replace(ApiContext apiContext, JsonObject response) {

    JsonObject newHeaders = new JsonObject();
    JsonObject headers = response.getJsonObject("headers", new JsonObject());
    for (String key : headers.fieldNames()) {
      Object newVal = apiContext.getValueByKeyword(headers.getValue(key));
      if (newVal != null) {
        newHeaders.put(key, newVal);
      }
    }
    response.put("headers", newHeaders);
    JsonObject newBody = new JsonObject();
    JsonObject body = response.getJsonObject("body");
    for (String key : body.fieldNames()) {
      Object newVal = apiContext.getValueByKeyword(body.getValue(key));
      if (newVal != null) {
        newBody.put(key, newVal);
      }
    }
    response.put("body", newBody);

  }


  private void tranformerHeaders(JsonObject headers,
                                 ResponseTransformerPlugin transformer) {
    transformer.headerRemoved().forEach(h -> headers.remove(h));
    transformer.headerReplaced().forEach(entry -> headers.put(entry.getKey(), entry.getValue()));
    transformer.headerAdded().forEach(entry -> headers.put(entry.getKey(), entry.getValue()));
  }

  private void tranformerBody(final JsonObject body,
                              ResponseTransformerPlugin transformer) {
    transformer.bodyRemoved().forEach(b -> body.remove(b));
    transformer.bodyReplaced().forEach(entry -> body.put(entry.getKey(), entry.getValue()));
    transformer.bodyAdded().forEach(entry -> body.put(entry.getKey(), entry.getValue()));
  }

}