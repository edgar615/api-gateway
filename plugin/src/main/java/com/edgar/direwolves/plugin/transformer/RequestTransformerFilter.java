package com.edgar.direwolves.plugin.transformer;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.rpc.HttpRpcRequest;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

/**
 * 将endpoint转换为json对象.
 * params和header的json均为{"k1", ["v1"]}，{"k1", ["v1", "v2]}格式的json对象.
 * <p>
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
 * <p>
 * Created by edgar on 16-9-20.
 */
public class RequestTransformerFilter implements Filter {

  RequestTransformerFilter() {
  }

  @Override
  public String type() {
    return PRE;
  }

  @Override
  public int order() {
    return 9500;
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
      HttpRpcRequest request = (HttpRpcRequest) apiContext.requests().get(i);
      transformer(apiContext, request);
    }
    completeFuture.complete(apiContext);
  }


  private void transformer(ApiContext apiContext, HttpRpcRequest request) {
    String name = request.name();
    RequestTransformerPlugin plugin =
            (RequestTransformerPlugin) apiContext.apiDefinition()
                    .plugin(RequestTransformerPlugin.class.getSimpleName());
    if (plugin == null) {
      return;
    }

    RequestTransformer transformer = plugin.transformer(name);
    if (transformer != null) {
      Multimap<String, String> params =  tranformerParams(request.getParams(), transformer);
      request.clearParams().addParams(params);
      Multimap<String, String> headers =tranformerHeaders(request.getHeaders(), transformer);
      request.clearHeaders().addHeaders(headers);
      if (request.getBody() != null) {
        JsonObject body = tranformerBody(request.getBody(), transformer);
        request.setBody(body);
      }
    }
  }

  private Multimap<String, String> tranformerParams(Multimap<String, String> params,
                                                    RequestTransformer transformer) {
    Multimap<String, String> newParams = ArrayListMultimap.create(params);
    transformer.paramRemoved().forEach(h -> newParams.removeAll(h));
    transformer.paramAdded().forEach(
        entry -> newParams.replaceValues(entry.getKey(), Lists.newArrayList(entry.getValue())));
    return newParams;
  }

  private Multimap<String, String> tranformerHeaders(Multimap<String, String> headers,
                                                     RequestTransformer transformer) {
    Multimap<String, String> newHeader = ArrayListMultimap.create(headers);
    transformer.headerRemoved().forEach(h -> newHeader.removeAll(h));
    transformer.headerAdded().forEach(
        entry -> newHeader.replaceValues(entry.getKey(), Lists.newArrayList(entry.getValue())));
    return newHeader;
  }

  private JsonObject tranformerBody(final JsonObject body,
                                    RequestTransformer transformer) {
    JsonObject newBody = body.copy();
    transformer.bodyRemoved().forEach(b -> newBody.remove(b));
    transformer.bodyAdded().forEach(entry -> newBody.put(entry.getKey(), entry.getValue()));
    return newBody;
  }


}