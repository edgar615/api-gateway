package com.github.edgar615.direwolves.http.filter;

import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.direwolves.core.rpc.RpcRequest;
import com.github.edgar615.direwolves.http.SdHttpEndpoint;
import com.github.edgar615.direwolves.http.SdHttpRequest;
import com.github.edgar615.direwolves.http.loadbalance.LoadBalance;
import com.github.edgar615.direwolves.http.loadbalance.LoadBalanceOptions;
import com.github.edgar615.direwolves.http.loadbalance.LoadBalanceStats;
import com.github.edgar615.direwolves.http.loadbalance.ServiceFinder;
import com.github.edgar615.util.log.Log;
import com.github.edgar615.util.vertx.task.Task;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * HTTP类型的endpoint需要经过这个Filter进行服务发现，找到对应的服务地址，并转换为RpcRequest.
 * <p>
 * 如果未找到对应的服务节点，不抛出异常(RpcRequest的record属性为null)，这样后面的降级模块就可以处理这种错误的降级
 *
 * @author Edgar  Date 2016/11/18
 */
public class ServiceDiscoveryFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceDiscoveryFilter.class);

  private final Vertx vertx;

  private final LoadBalance loadBalance;

  ServiceDiscoveryFilter(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    JsonObject discoveryConfig = config.getJsonObject("service.discovery", new JsonObject());
    ServiceDiscoveryOptions options = new ServiceDiscoveryOptions(discoveryConfig);
    ServiceFinder serviceFinder = ServiceFinder.create(vertx,
                                                       ServiceDiscovery.create(vertx, options));
    JsonObject loadBalanceConfig = config.getJsonObject("load.balance", new JsonObject());
    LoadBalanceOptions loadBalanceOptions = new LoadBalanceOptions(loadBalanceConfig);
    loadBalance = LoadBalance.create(serviceFinder, loadBalanceOptions);
    JsonObject circuitConfig = config.getJsonObject("circuit.breaker", new JsonObject());
    String stateAnnounce =
            circuitConfig.getString("state.announce", "direwolves.circuitbreaker.announce");
    vertx.eventBus().<JsonObject>consumer(stateAnnounce, msg -> {
      JsonObject jsonObject = msg.body();
      String serverId = jsonObject.getString("name");
      String state = jsonObject.getString("state");
      if ("open".equalsIgnoreCase(state)) {
        LoadBalanceStats.instance().get(serverId).setCircuitBreakerTripped(true);
      }
      if ("close".equalsIgnoreCase(state)) {
        LoadBalanceStats.instance().get(serverId).setCircuitBreakerTripped(false);
      }
      if ("halfOpen".equalsIgnoreCase(state)) {
        LoadBalanceStats.instance().get(serverId).setCircuitBreakerTripped(false);
      }
    });
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
    return apiContext.apiDefinition().endpoints().stream()
            .anyMatch(e -> e instanceof SdHttpEndpoint);
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    List<Future<Record>> futures =
            apiContext.apiDefinition().endpoints().stream()
                    .filter(e -> e instanceof SdHttpEndpoint)
                    .map(e -> ((SdHttpEndpoint) e).service())
                    .distinct()
                    .map(r -> serviceFuture(apiContext.id(), r))
                    .filter(r -> r != null)
                    .collect(Collectors.toList());


    Task.par(futures)
            .andThen(records -> {
              apiContext.apiDefinition().endpoints().stream()
                      .filter(e -> e instanceof SdHttpEndpoint)
                      .map(e -> (SdHttpEndpoint) e)
                      .map(e -> toRpc(apiContext, e, records))
                      .forEach(req -> apiContext.addRequest(req));
            })
            .andThen(records -> {
              completeFuture.complete(apiContext);
            })
            .onFailure(throwable -> completeFuture.fail(throwable));

  }


  private Future<Record> serviceFuture(String traceId, String service) {
    Future<Record> future = Future.future();
    loadBalance.chooseServer(service, ar -> {
      if (ar.failed()) {
        Log.create(LOGGER)
                .setModule("Filter")
                .setTraceId(traceId)
                .setEvent("service.undiscovered")
                .addData("service", service)
                .warn();
//        future.fail(SystemException.create(DefaultErrorCode.SERVICE_UNAVAILABLE)
//                            .set("details", "Service not found: " + service));
        future.complete(null);
        return;
      }
      Record record = ar.result();
      if (record == null) {
//        future.fail(SystemException.create(DefaultErrorCode.SERVICE_UNAVAILABLE)
//                            .set("details", "Service not found: " + service));
        future.complete(null);
        return;
      }
      Log.create(LOGGER)
              .setModule("Filter")
              .setTraceId(traceId)
              .setEvent("service.discovered")
              .addData("service", service)
              .setMessage("[{}]")
              .addArg(ar.result().toJson())
              .info();
      future.complete(record);
    });
    return future;

  }

  private RpcRequest toRpc(ApiContext apiContext, SdHttpEndpoint endpoint,
                           List<Record> records) {
    SdHttpRequest httpRpcRequest =
            SdHttpRequest.create(apiContext.nextRpcId(), endpoint.name());
    httpRpcRequest.setPath(endpoint.path());
    httpRpcRequest.setHttpMethod(endpoint.method());
    httpRpcRequest.addParams(apiContext.params());
//    httpRpcRequest.addHeaders(apiContext.headers());
    httpRpcRequest.addHeader("x-request-id", httpRpcRequest.id());
    httpRpcRequest.setBody(apiContext.body());
    List<Record> instances = records.stream()
            .filter(r -> r != null)
            .filter(r -> endpoint.service().equalsIgnoreCase(r.getName()))
            .collect(Collectors.toList());
    if (instances.isEmpty()) {
      Log.create(LOGGER)
              .setTraceId(apiContext.id())
              .setModule("Filter")
              .setEvent("service.discovery.failed")
              .setMessage("service:{} not found")
              .addArg(endpoint.name())
              .error();
      return httpRpcRequest;
//      throw SystemException.create(DefaultErrorCode.SERVICE_UNAVAILABLE)
//              .set("details", "Service not found, endpoint:" + endpoint.name());
    }
    Record instance = instances.get(0);
    httpRpcRequest.setRecord(new Record(instance));
    return httpRpcRequest;
  }

}
