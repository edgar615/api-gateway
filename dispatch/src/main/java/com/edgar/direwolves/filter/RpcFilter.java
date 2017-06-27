package com.edgar.direwolves.filter;

import com.google.common.collect.Lists;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.rpc.FailureRpcHandler;
import com.edgar.direwolves.core.rpc.RpcHandler;
import com.edgar.direwolves.core.rpc.RpcHandlerFactory;
import com.edgar.direwolves.core.rpc.RpcMetric;
import com.edgar.direwolves.core.rpc.RpcResponse;
import com.edgar.direwolves.core.rpc.http.HttpRpcRequest;
import com.edgar.util.vertx.task.Task;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 该filter用于将请求参数中的带变量用真实值替换.
 * 该filter的order=2147483647，int的最大值.
 * <b>params和headers中的所有值都是String</b>
 * 对于params和headers，如果新值是集合或者数组，将集合或数组的元素一个个放入params或headers，而不是将一个集合直接放入.(不考虑嵌套的集合)
 * 例如：q1 : $header.h1对应的值是[h1.1, h1.2]，那么最终替换之后的新值是 q1 : [h1.1,h1.2]而不是 q1 : [[h1.1,h1.2]]
 */
public class RpcFilter extends RequestReplaceFilter implements Filter {

  /**
   * RPC处理类的MAP对象，key为RPC的类型，value为RPC的处理类.
   */
  private final Map<String, RpcHandler> handlers = new ConcurrentHashMap();

  /**
   * 未定义的RPC类型直接返回异常
   */
  private final RpcHandler failureRpcHandler = FailureRpcHandler.create("Undefined Rpc");

  RpcFilter(Vertx vertx, JsonObject config) {

    RpcMetric metric = null;

    Lists.newArrayList(ServiceLoader.load(RpcHandlerFactory.class))
            .stream().map(f -> f.create(vertx, config, metric))
            .forEach(h -> handlers.put(h.type().toUpperCase(), h));;
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
    List<Future<RpcResponse>> futures = apiContext.requests()
            .stream().map(req -> handlers.getOrDefault(req.type().toUpperCase(), failureRpcHandler)
                    .handle(req))
            .collect(Collectors.toList());
    Task.par(futures).andThen(responses -> {
      for (RpcResponse response : responses) {
        apiContext.addResponse(response);
      }
      completeFuture.complete(apiContext);
    }).onFailure(throwable -> completeFuture.fail(throwable));
  }

}