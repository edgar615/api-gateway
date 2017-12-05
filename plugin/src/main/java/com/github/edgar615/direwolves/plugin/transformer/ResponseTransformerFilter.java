package com.github.edgar615.direwolves.plugin.transformer;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.direwolves.core.dispatch.Result;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.Collection;

/**
 * 将Result中的请求头，请求参数，请求体按照ResponseTransformerPlugin中的配置处理.
 * 目前body只考虑JsonObject类型的result修改，对JsonArray暂不支持.
 * <p>
 * 执行的顺序为: remove add
 * 该filter的order=1000
 * <p>
 * 接受的参数
 * <p>
 * "response.transformer": {
 * "header.add": [
 * "x-auth-userId:$user.userId",
 * "x-auth-companyCode:$user.companyCode",
 * "x-policy-owner:individual"
 * ],
 * "header.remove": [
 * "Authorization"
 * ],
 * "header.replace": [
 * "x-app-verion:x-client-version"
 * ],
 * "body.add": [
 * "userId:$user.userId"
 * ],
 * "body.remove": [
 * "appKey",
 * "nonce"
 * ],
 * "body.replace": [
 * "x-app-verion:x-client-version"
 * ]
 * }
 * Created by edgar on 16-9-20.
 */
public class ResponseTransformerFilter implements Filter {

  private final ResponseTransformerPlugin globalPlugin;// = new ResponseTransformerPluginImpl();

  ResponseTransformerFilter(JsonObject config) {
    JsonObject jsonObject = config.getJsonObject("response.transformer", new JsonObject());
    if (jsonObject.isEmpty()) {
      globalPlugin = null;
    } else {
      globalPlugin = new ResponseTransformerPluginImpl();
      ResponseTransformerConverter.fromJson(jsonObject, globalPlugin);
    }
  }

  @Override
  public String type() {
    return POST;
  }

  @Override
  public int order() {
    return 10000;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    if (apiContext.apiDefinition() == null) {
      return false;
    }
    return globalPlugin != null
           || apiContext.apiDefinition()
                      .plugin(ResponseTransformerPlugin.class.getSimpleName()) != null;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {

    if (globalPlugin != null) {
      doTransfomer(apiContext, globalPlugin);
    }
    ResponseTransformerPlugin plugin =
            (ResponseTransformerPlugin) apiContext.apiDefinition()
                    .plugin(ResponseTransformerPlugin.class.getSimpleName());

    if (plugin != null) {
      doTransfomer(apiContext, plugin);
    }
    completeFuture.complete(apiContext);
  }

  private void doTransfomer(ApiContext apiContext, ResponseTransformerPlugin plugin) {
    Result result = apiContext.result();
    //如果body的JsonObject直接添加，如果是JsonArray，不支持body的修改
    boolean isArray = result.isArray();
    //body目前仅考虑JsonObject的替换
    Multimap<String, String> header =
            tranformerHeaders(result.headers(), plugin);
    if (!isArray) {
      JsonObject body = tranformerBody(result.responseObject(), plugin);
      apiContext.setResult(Result.createJsonObject(result.statusCode(),
                                                   body, header));
    } else {
      apiContext.setResult(Result.createJsonArray(result.statusCode(),
                                                  result.responseArray(), header));
    }
  }

  private Multimap<String, String> tranformerHeaders(Multimap<String, String> headers,
                                                     ResponseTransformerPlugin transformer) {
    Multimap<String, String> newHeader = ArrayListMultimap.create(headers);
    transformer.headerRemoved().forEach(h -> newHeader.removeAll(h));
    transformer.headerReplaced().forEach(entry -> {
      if (newHeader.containsKey(entry.getKey())) {
        Collection<String> values = newHeader.removeAll(entry.getKey());
        newHeader.putAll(entry.getValue(), values);
      }
    });
    transformer.headerAdded().forEach(
            entry -> newHeader.put(entry.getKey(), entry.getValue()));
    return newHeader;
  }

  private JsonObject tranformerBody(final JsonObject body,
                                    ResponseTransformerPlugin transformer) {
    JsonObject newBody = body.copy();
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