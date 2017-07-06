package com.edgar.direwolves.filter;

import com.google.common.collect.Lists;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.rpc.FailureRpcHandler;
import com.edgar.direwolves.core.rpc.RpcHandler;
import com.edgar.direwolves.core.rpc.RpcHandlerFactory;
import com.edgar.direwolves.core.rpc.RpcMetric;
import com.edgar.direwolves.core.rpc.RpcRequest;
import com.edgar.direwolves.core.rpc.RpcResponse;
import com.edgar.direwolves.core.rpc.http.HttpRpcRequest;
import com.edgar.util.vertx.task.Task;
import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 执行RPC调用。该Filter应该放在PRE类型的最后面或者POST类型的最前面.
 */
public class RpcFilter extends RequestReplaceFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(RpcFilter.class);

  /**
   * RPC处理类的MAP对象，key为RPC的类型，value为RPC的处理类.
   */
  private final Map<String, RpcHandler> handlers = new ConcurrentHashMap();

  /**
   * 未定义的RPC类型直接返回异常
   */
  private final RpcHandler failureRpcHandler = FailureRpcHandler.create("Undefined Rpc");

  private final Map<String, CircuitBreakerRegistry> breakerMap;

  private final Vertx vertx;

  RpcFilter(Vertx vertx, JsonObject config) {

    this.vertx = vertx;
    RpcMetric metric = null;

    Lists.newArrayList(ServiceLoader.load(RpcHandlerFactory.class))
            .stream().map(f -> f.create(vertx, config, metric))
            .forEach(h -> handlers.put(h.type().toUpperCase(), h));

    this.breakerMap = vertx.sharedData().getLocalMap("circuit.breaker.registry");
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
            .stream().map(req -> circuitBreakerExecute(req))
            .collect(Collectors.toList());
    Task.par(futures).andThen(responses -> {
      for (RpcResponse response : responses) {
        apiContext.addResponse(response);
      }
      completeFuture.complete(apiContext);
    }).onFailure(throwable -> completeFuture.fail(throwable));
  }

  private Future<RpcResponse> circuitBreakerExecute(RpcRequest req) {
    if (req instanceof HttpRpcRequest) {
      Future<RpcResponse> future = Future.future();
      HttpRpcRequest httpRpcRequest = (HttpRpcRequest) req;
      CircuitBreakerRegistry registry
              = breakerMap.putIfAbsent(httpRpcRequest.serverId(),
                                       new CircuitBreakerRegistry(vertx,
                                                                  httpRpcRequest.serverId()));
      if (registry == null) {
        registry = breakerMap.get(httpRpcRequest.serverId());
      }
      CircuitBreaker circuitBreaker = registry.get();
      circuitBreaker.<RpcResponse>execute(f -> {
        RpcHandler handler =
                handlers.getOrDefault(httpRpcRequest.type().toUpperCase(), failureRpcHandler);
        handler.handle(req)
                .setHandler(f.completer());
      }).setHandler(ar -> {
        if (ar.failed()) {
          if (ar.cause() instanceof RuntimeException
              && "open circuit".equals(ar.cause().getMessage())) {
            LOGGER.warn("---| [{}] [FAILED] [{}] [{}]",
                        httpRpcRequest.id(),
                        this.getClass().getSimpleName(),
                        "BreakerTripped");
          }
          future.fail(ar.cause());
        } else {
          future.complete(ar.result());
        }
      });

      return future;
    } else {
      return handlers.getOrDefault(req.type().toUpperCase(), failureRpcHandler).handle(req);
    }

  }

}