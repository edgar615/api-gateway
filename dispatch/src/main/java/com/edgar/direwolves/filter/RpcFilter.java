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
import com.edgar.direwolves.plugin.fallback.CircuitFallbackPlugin;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.vertx.task.Task;
import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.impl.NoStackTraceThrowable;
import io.vertx.core.json.JsonArray;
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
 * <p>
 * 接受的参数
 * <p>
 * "circuit.breaker" : {
 * "maxFailures" : 5,
 * "maxRetries" : 0,
 * "resetTimeout" : 60000,
 * "timeout" : 3000,
 * "metricsRollingWindow" : 10000,
 * "notificationPeriod" : 2000,
 * "notificationAddress" : "vertx.circuit-breaker",
 * "registry" : "vertx.circuit.breaker.registry"
 * }
 * <p>
 * - maxFailures  针对一个服务的请求失败多少次之后开启断路器，默认值5
 * - maxRetries 请求失败后的重试次数，默认值0
 * - resetTimeout 断路器打开之后，等待多长时间重置为半开状态，单位毫秒，默认值30000
 * - timeout 一个请求多长时间没有返回任务超时（失败）， 单位毫秒，默认值10000
 * - metricsRollingWindow 度量的时间窗口 单位毫秒，默认值10000
 * - notificationPeriod  通知周期，单位毫秒，默认值2000
 * - notificationAddress  通知地址，默认值vertx.circuit-breaker
 * - registry localmap中保存断路器的键值，默认值vertx.circuit.breaker.registry
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

  private JsonObject config;

  RpcFilter(Vertx vertx, JsonObject config) {

    this.vertx = vertx;
    RpcMetric metric = null;
    this.config = config.getJsonObject("circuit.breaker", new JsonObject());

    Lists.newArrayList(ServiceLoader.load(RpcHandlerFactory.class))
            .stream().map(f -> f.create(vertx, config, metric))
            .forEach(h -> handlers.put(h.type().toUpperCase(), h));

    this.breakerMap = vertx.sharedData()
            .getLocalMap(config.getString("registry",
                                          "vertx.circuit.breaker.registry"));
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
    if (breakerMap.size() % 1000 == 0) {
      breakerMap.clear();
    }
    List<Future<RpcResponse>> futures = apiContext.requests()
            .stream().map(req -> execute(apiContext, req))
            .collect(Collectors.toList());
    Task.par(futures).andThen(responses -> {
      for (RpcResponse response : responses) {
        apiContext.addResponse(response);
      }
      completeFuture.complete(apiContext);
    }).onFailure(throwable -> completeFuture.fail(throwable));
  }

  private Future<RpcResponse> execute(ApiContext apiContext, RpcRequest req) {
    if (req instanceof HttpRpcRequest) {
      Future future = Future.future();
      circuitBreakerExecute(apiContext, (HttpRpcRequest) req)
              .setHandler(ar -> {
                if (ar.failed()) {
                  if (ar.cause() instanceof NoStackTraceThrowable
                      && "operation timeout".equals(ar.cause().getMessage())) {
                    future.fail(SystemException.create(DefaultErrorCode.TIME_OUT)
                                        .set("timeout", config.getLong("timeout", 10000l)));
                    return;
                  }
                  if (ar.cause() instanceof RuntimeException
                      && "open circuit".equals(ar.cause().getMessage())) {
                    LOGGER.warn("---| [{}] [FAILED] [{}] [{}]",
                                req.id(),
                                this.getClass().getSimpleName(),
                                "BreakerTripped");
                    future.fail(SystemException.create(DefaultErrorCode.BREAKER_TRIPPED)
                                        .set("details", String.format("Please try again after %dms",
                                                                      config.getLong("resetTimeout",
                                                                                     30000l))));
                    return;
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

  private Future<RpcResponse> circuitBreakerExecute(ApiContext apiContext, HttpRpcRequest req) {
    CircuitBreakerRegistry registry
            = breakerMap.putIfAbsent(req.serverId(),
                                     new CircuitBreakerRegistry(vertx, req.serverId(),
                                                                new CircuitBreakerOptions(
                                                                        config)));
    if (registry == null) {
      registry = breakerMap.get(req.serverId());
    }
    CircuitBreaker circuitBreaker = registry.get();
    long start = System.currentTimeMillis();
    Future<RpcResponse> rpcFuture
            = handlers
            .getOrDefault(req.type().toUpperCase(), failureRpcHandler)
            .handle(req);
    CircuitFallbackPlugin plugin
            = (CircuitFallbackPlugin) apiContext.apiDefinition()
            .plugin(CircuitFallbackPlugin.class.getSimpleName());
    if (plugin != null && plugin.fallback().containsKey(req.name())) {
      return circuitBreaker.<RpcResponse>executeWithFallback(
              f -> rpcFuture.setHandler(f.completer())
              , throwable -> {
                long duration = System.currentTimeMillis() - start;
                Object fallback = plugin.fallback().getValue(req.name());
                if (fallback instanceof JsonObject) {
                  return RpcResponse
                          .createJsonObject(req.id(), 200, (JsonObject) fallback, duration);
                }
                if (fallback instanceof JsonArray) {
                  return RpcResponse
                          .createJsonArray(req.id(), 200, (JsonArray) fallback, duration);
                }
                return null;
              });
    } else {
      return circuitBreaker.<RpcResponse>execute(f -> rpcFuture.setHandler(f.completer()));
    }

  }

}