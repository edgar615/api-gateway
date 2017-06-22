package com.edgar.direwolves.plugin.transformer;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.rpc.RpcRequest;
import com.edgar.direwolves.core.rpc.http.HttpRpcRequest;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

import java.util.Collection;

/**
 * 将RpcRequest中的请求头，请求参数，请求体按照RequestTransformerPlugin中的配置处理.
 * <p>
 * 执行的顺序为: remove replace add
 * <p>
 * 该filter的order=15000
 * Created by edgar on 16-9-20.
 */
public class RequestTransformerFilter implements Filter {

  private final RequestTransformer globalTransfomer = RequestTransformer.create("global");

  RequestTransformerFilter(JsonObject config) {
    JsonObject jsonObject = config.getJsonObject("request_transformer", new JsonObject());
    RequestTransfomerConverter.fromJson(jsonObject, globalTransfomer);
  }

  @Override
  public String type() {
    return PRE;
  }

  @Override
  public int order() {
    return 15000;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    if (apiContext.apiDefinition() == null) {
      return false;
    }
    return   apiContext.requests().size() > 0
             && apiContext.requests().stream()
                   .anyMatch(e -> e instanceof HttpRpcRequest);
//    return apiContext.apiDefinition()
//                   .plugin(RequestTransformerPlugin.class.getSimpleName()) != null
//           && apiContext.requests().size() > 0;
//           && apiContext.requests().stream()
//                   .anyMatch(e -> e instanceof HttpRpcRequest);
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    for (int i = 0; i < apiContext.requests().size(); i++) {
      RpcRequest request = apiContext.requests().get(i);
      if (request instanceof HttpRpcRequest) {
        doTransformer((HttpRpcRequest) request, globalTransfomer);
        transformer(apiContext, (HttpRpcRequest) request);
      }
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
      doTransformer(request, transformer);
    }
  }

  private void doTransformer(HttpRpcRequest request, RequestTransformer transformer) {
    Multimap<String, String> params = tranformerParams(request.params(), transformer);
    request.clearParams().addParams(params);
    Multimap<String, String> headers = tranformerHeaders(request.headers(), transformer);
    request.clearHeaders().addHeaders(headers);
    if (request.method() == HttpMethod.POST
        || request.method() == HttpMethod.PUT) {
      JsonObject body = tranformerBody(request.body(), transformer);
      request.setBody(body);
    }
  }

  private Multimap<String, String> tranformerParams(Multimap<String, String> params,
                                                    RequestTransformer transformer) {
    Multimap<String, String> newParams = ArrayListMultimap.create(params);
    transformer.paramRemoved().forEach(h -> newParams.removeAll(h));
    transformer.paramReplaced().forEach(entry -> {
      if (newParams.containsKey(entry.getKey())) {
        Collection<String> values = newParams.removeAll(entry.getKey());
        newParams.putAll(entry.getValue(), values);
      }
    });
    transformer.paramAdded().forEach(
            entry -> newParams.replaceValues(entry.getKey(), Lists.newArrayList(entry.getValue())));
    return newParams;
  }

  private Multimap<String, String> tranformerHeaders(Multimap<String, String> headers,
                                                     RequestTransformer transformer) {
    Multimap<String, String> newHeader = ArrayListMultimap.create(headers);
    transformer.headerRemoved().forEach(h -> newHeader.removeAll(h));
    transformer.headerReplaced().forEach(entry -> {
      if (newHeader.containsKey(entry.getKey())) {
        Collection<String> values = newHeader.removeAll(entry.getKey());
        newHeader.putAll(entry.getValue(), values);
      }
    });
    transformer.headerAdded().forEach(
            entry -> newHeader.replaceValues(entry.getKey(), Lists.newArrayList(entry.getValue())));
    return newHeader;
  }

  private JsonObject tranformerBody(final JsonObject body,
                                    RequestTransformer transformer) {
    final JsonObject newBody = new JsonObject();
    if (body != null) {
      newBody.mergeIn(body.copy());
    }
    transformer.bodyRemoved().forEach(b -> newBody.remove(b));
    transformer.bodyReplaced().forEach(entry -> {
      if (newBody.containsKey(entry.getKey())) {
        Object value = newBody.remove(entry.getKey());
        newBody.put(entry.getValue(), value);
      }
    });
    transformer.bodyAdded().forEach(entry -> newBody.put(entry.getKey(), entry.getValue()));
    return newBody;
  }


}