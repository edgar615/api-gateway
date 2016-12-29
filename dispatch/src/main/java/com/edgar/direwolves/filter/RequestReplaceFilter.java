package com.edgar.direwolves.filter;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.rpc.http.HttpRpcRequest;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class RequestReplaceFilter implements Filter {
  RequestReplaceFilter() {
  }

  @Override
  public String type() {
    return PRE;
  }

  @Override
  public int order() {
    return 10000;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    return true;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    for (int i = 0; i < apiContext.requests().size(); i++) {
      HttpRpcRequest request = (HttpRpcRequest) apiContext.requests().get(i);
      replace(apiContext, request);
    }
    completeFuture.complete(apiContext);
  }

  private void replace(ApiContext apiContext, HttpRpcRequest request) {
    Multimap<String, String> params = replaceHeader(apiContext, apiContext.params());
    Multimap<String, String> headers = replaceHeader(apiContext, apiContext.headers());
    request.clearHeaders().addHeaders(headers);
    request.clearParams().addParams(params);
    if (request.getBody() != null) {
      JsonObject body = replaceBody(apiContext, request.getBody());
      request.setBody(body);
    }

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


}