package com.github.edgar615.gateway.filter;

import com.google.common.collect.Multimap;

import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.gateway.core.dispatch.Filter;
import com.github.edgar615.gateway.core.dispatch.Result;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

/**
 * 将Result中的请求头，请求参数，请求体按照ResponseTransformerPlugin中的配置处理.
 * 目前body只考虑JsonObject类型的result修改，对JsonArray暂不支持.
 * Created by edgar on 16-9-20.
 */
public class ResponseReplaceFilter extends AbstractReplaceFilter implements Filter {

  ResponseReplaceFilter() {
  }

  @Override
  public String type() {
    return POST;
  }

  @Override
  public int order() {
    return Integer.MAX_VALUE - 1000;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    return true;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    Result result = apiContext.result();
    //如果body的JsonObject直接添加，如果是JsonArray，不支持body的修改
    boolean isArray = result.isArray();
    //body目前仅考虑JsonObject的替换
    Multimap<String, String> header =
            replaceHeader(apiContext, result.headers());

    if (!isArray) {
      JsonObject body = replaceBody(apiContext, result.responseObject());
      apiContext.setResult(Result.createJsonObject(result.statusCode(),
                                                   body, header));
    } else {
      apiContext.setResult(Result.createJsonArray(result.statusCode(),
                                                  result.responseArray(), header));
    }
    completeFuture.complete(apiContext);
  }

}