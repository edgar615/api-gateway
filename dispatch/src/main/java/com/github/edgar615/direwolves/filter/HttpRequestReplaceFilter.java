package com.github.edgar615.direwolves.filter;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Multimap;

import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.direwolves.core.rpc.RpcRequest;
import com.github.edgar615.direwolves.core.rpc.http.HttpRpcRequest;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 该filter用于将请求参数中的带变量用真实值替换.
 * 该filter的order=2147483647，int的最大值.
 * <b>params和headers中的所有值都是String</b>
 * 对于params和headers，如果新值是集合或者数组，将集合或数组的元素一个个放入params或headers，而不是将一个集合直接放入.(不考虑嵌套的集合)
 * 例如：q1 : $header.h1对应的值是[h1.1, h1.2]，那么最终替换之后的新值是 q1 : [h1.1,h1.2]而不是 q1 : [[h1.1,h1.2]]
 */
public class HttpRequestReplaceFilter extends RequestReplaceFilter implements Filter {
  HttpRequestReplaceFilter() {
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    return apiContext.requests().stream()
            .anyMatch(e -> e instanceof HttpRpcRequest);
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    for (int i = 0; i < apiContext.requests().size(); i++) {
      RpcRequest request = apiContext.requests().get(i);
      if (request instanceof HttpRpcRequest) {
        replace(apiContext, (HttpRpcRequest) request);
      }
    }
    completeFuture.complete(apiContext);
  }

  private void replace(ApiContext apiContext, HttpRpcRequest request) {
    Multimap<String, String> params = replaceHeader(apiContext, request.params());
    Multimap<String, String> headers = replaceHeader(apiContext, request.headers());
    request.clearHeaders().addHeaders(headers);
    request.clearParams().addParams(params);
    if (request.body() != null) {
      JsonObject body = replaceBody(apiContext, request.body());
      request.setBody(body);
    }
    String path = request.path();
    List<String> pathList = Splitter.on("/").omitEmptyStrings().trimResults().splitToList(path);
    List<String> newPath = new ArrayList<>();
    for (String p : pathList) {
      if (!p.startsWith("$")) {
        newPath.add(p);
      } else {
        Object newVal = apiContext.getValueByKeyword(p);
        if (newVal == null) {
          throw SystemException.create(DefaultErrorCode.NULL)
                  .set("details", p + " undefined");
        }
        if (newVal instanceof List) {
          List valList = (List) newVal;
          newPath.add(valList.get(0).toString());
        } else if (newVal instanceof JsonArray) {
          JsonArray valList = (JsonArray) newVal;
          newPath.add(valList.getValue(0).toString());
        } else {
          newPath.add(newVal.toString());
        }
      }
    }
    request.setPath("/" + Joiner.on("/").join(newPath));

  }

}