package com.github.edgar615.direwolves.http.filter;

import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.direwolves.core.rpc.RpcRequest;
import com.github.edgar615.util.log.Log;
import com.github.edgar615.direwolves.http.SdHttpEndpoint;
import com.github.edgar615.direwolves.http.SdHttpRequest;
import com.github.edgar615.direwolves.http.loadbalance.*;
import com.github.edgar615.direwolves.http.splitter.ServiceSplitterPlugin;
import com.github.edgar615.direwolves.http.splitter.ServiceTraffic;
import com.github.edgar615.util.vertx.task.Task;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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

  private final ServiceFilter circuitBreakerFilter = r ->
          !LoadBalanceStats.instance().get(r.getRegistration()).isCircuitBreakerTripped();

  ServiceDiscoveryFilter(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    JsonObject discoveryConfig = config.getJsonObject("service.discovery", new JsonObject());
    ServiceDiscoveryOptions options = new ServiceDiscoveryOptions(discoveryConfig);
    ServiceFinder serviceFinder = ServiceFinder.create(vertx,
                                                       ServiceDiscovery.create(vertx, options));
    JsonObject loadBalanceConfig = config.getJsonObject("load.balance", new JsonObject());
    LoadBalanceOptions loadBalanceOptions = new LoadBalanceOptions(loadBalanceConfig);
    loadBalance = LoadBalance.create(serviceFinder, loadBalanceOptions);
    //监听断路器变化，更新服务节点的断路器状态
    JsonObject circuitConfig = config.getJsonObject("circuit.breaker", new JsonObject());
    String stateAnnounce =
            circuitConfig.getString("stateAnnounce", "__com.github.edgar615.direwolves.circuitbreaker.announce");
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
    return 13000;
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
                    .map(r -> serviceFuture(apiContext, r))
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


  private Future<Record> serviceFuture(ApiContext apiContext, String service) {
    Future<Record> future = Future.future();
    List<ServiceFilter> serviceFilters = new ArrayList<>();
    serviceFilters.add(circuitBreakerFilter);
    ServiceSplitterPlugin serviceSplitterPlugin = (ServiceSplitterPlugin) apiContext.apiDefinition().plugin(ServiceSplitterPlugin.class.getSimpleName());
    if (serviceSplitterPlugin != null && serviceSplitterPlugin.traffic(service) != null) {
      ServiceTraffic traffic = serviceSplitterPlugin.traffic(service);
      String tag = traffic.decision(apiContext);
      serviceFilters.add(r -> r.getMetadata().getJsonArray("ServiceTags").contains(tag));
    }
    //todo 处理tag未找到时，向默认服务转发的逻辑
    loadBalance.chooseServer(service, serviceFilters, ar -> {
      if (ar.failed() || ar.result() == null) {
        Log.create(LOGGER)
                .setTraceId(apiContext.id())
                .setEvent("ServiceNonExistent")
                .addData("service", service)
                .warn();
        future.complete(null);
        return;
      }
      future.complete(ar.result());
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
      return httpRpcRequest;
    }
    Record instance = instances.get(0);
    httpRpcRequest.setRecord(new Record(instance));
    return httpRpcRequest;
  }

}
