package com.edgar.direwolves.filter;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.rpc.RpcRequest;
import com.edgar.direwolves.core.rpc.http.HttpRpcRequest;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 该filter用于将请求参数中的带变量用真实值替换.
 * 该filter的order=2147483647，int的最大值.
 * <p>
 * 仅支持单层替换，即如果body.obj的属性为<code>{"userId":1,"h1":"$header.h1"}</code>
 * 那么$body.obj属性仅会返回<code>{"userId":1,"h1":"$header.h1"}</code>，不会继续对<code>$header.h1</code>处理
 * <p>
 * <b>params和headers中的所有值都是String</b>
 * 对于params和headers，如果新值是集合或者数组，将集合或数组的元素一个个放入params或headers，而不是将一个集合直接放入.
 * 例如：q1 : $header.h1对应的值是[h1.1, h1.2]，那么最终替换之后的新值是 q1 : [h1.1,h1.2]而不是 q1 : [[h1.1,h1.2]]
 */
public class HttpRequestReplaceFilter extends RequestReplaceFilter implements Filter {
  HttpRequestReplaceFilter() {
  }

  @Override
  public String type() {
    return PRE;
  }

  @Override
  public int order() {
    return Integer.MAX_VALUE;
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