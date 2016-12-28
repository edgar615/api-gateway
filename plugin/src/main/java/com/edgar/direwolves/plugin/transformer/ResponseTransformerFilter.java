package com.edgar.direwolves.plugin.transformer;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.dispatch.Result;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * response_transfomer.
 * <p>
 * </pre>
 * <p>
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

    Result result = apiContext.result();
    //如果body的JsonObject直接添加，如果是JsonArray，不支持body的修改
    boolean isArray = result.isArray();
    //body目前仅考虑JsonObject的替换
    Multimap<String, String> header =
            replaceHeader(apiContext, tranformerHeaders(result.header(), plugin));

    if (!isArray) {
      JsonObject body = replaceBody(apiContext, tranformerBody(result.responseObject(), plugin));
      apiContext.setResult(Result.createJsonObject(result.statusCode(),
                                                   body, header));
    } else {
      apiContext.setResult(Result.createJsonArray(result.statusCode(),
                                                  result.responseArray(), header));
    }
    completeFuture.complete(apiContext);
  }

  private Multimap<String, String> replaceHeader(ApiContext apiContext,
                                                 Multimap<String, String> headers) {
    Multimap<String, String> newHeaders = ArrayListMultimap.create();
    for (String key : headers.keySet()) {
      List<String> values = new ArrayList<>(headers.get(key));
      for (String val : values) {
        Object newVal = apiContext.getValueByKeyword(val);
        if (newVal != null) {
          newHeaders.put(key, newVal.toString());
        }
      }
    }
    return newHeaders;

  }

  private JsonObject replaceBody(ApiContext apiContext, JsonObject body) {
    JsonObject newBody = new JsonObject();
    if (body != null) {
      for (String key : body.fieldNames()) {
        Object newVal = apiContext.getValueByKeyword(body.getValue(key));
        if (newVal != null) {
          newBody.put(key, newVal);
        }
      }
    }
    return newBody;

  }


  private Multimap<String, String> tranformerHeaders(Multimap<String, String> headers,
                                                     ResponseTransformerPlugin transformer) {
    Multimap<String, String> newHeader = ArrayListMultimap.create(headers);
    transformer.headerRemoved().forEach(h -> newHeader.removeAll(h));
    transformer.headerAdded().forEach(
            entry -> newHeader.replaceValues(entry.getKey(), Lists.newArrayList(entry.getValue())));
    return newHeader;
  }

  private JsonObject tranformerBody(final JsonObject body,
                                    ResponseTransformerPlugin transformer) {
    JsonObject newBody = body.copy();
    transformer.bodyRemoved().forEach(b -> newBody.remove(b));
    transformer.bodyAdded().forEach(entry -> newBody.put(entry.getKey(), entry.getValue()));
    return newBody;
  }

}