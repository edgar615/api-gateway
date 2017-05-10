package com.edgar.direwolves.filter;

import com.edgar.direwolves.core.definition.Endpoint;
import com.edgar.direwolves.core.definition.HttpEndpoint;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.rpc.RpcRequest;
import com.edgar.direwolves.core.rpc.http.HttpRpcRequest;
import com.edgar.direwolves.core.utils.Helper;
import com.edgar.direwolves.discovery.ServiceInstance;
import com.edgar.direwolves.discovery.ServiceDiscovery;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.vertx.task.Task;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * HTTP类型的endpoint需要经过这个Filter进行服务发现，找到对应的服务地址，并转换为RpcRequest.
 *
 * @author Edgar  Date 2016/11/18
 */
public class ServiceDiscoveryFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceDiscoveryFilter.class);

  private final ServiceDiscovery serviceDiscovery;

  private final Vertx vertx;

  ServiceDiscoveryFilter(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    this.serviceDiscovery = ServiceDiscovery.create(vertx, config);
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
            .anyMatch(e -> e instanceof HttpEndpoint);
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    List<ServiceInstance> instances =
            apiContext.apiDefinition().endpoints().stream()
                    .filter(e -> e instanceof HttpEndpoint)
                    .map(e -> ((HttpEndpoint) e).service())
                    .distinct()
                    .map(s -> serviceFuture(s))
                    .collect(Collectors.toList());

    apiContext.apiDefinition().endpoints().stream()
            .filter(e -> e instanceof HttpEndpoint)
            .map(e -> toRpc(apiContext, e, instances))
            .forEach(req -> apiContext.addRequest(req));

    completeFuture.complete(apiContext);

  }

  private ServiceInstance serviceFuture(String service) {
    //服务发现
    try {
      ServiceInstance instance = serviceDiscovery.getInstance(service);
      if (instance == null) {
        throw SystemException.create(DefaultErrorCode.SERVICE_UNAVAILABLE)
                .set("details", "Service not found: " + service);
      }
      return instance;
    } catch (Exception e) {
      throw SystemException.create(DefaultErrorCode.SERVICE_UNAVAILABLE)
              .set("details", "Service not found: " + service);
    }
  }

  private RpcRequest toRpc(ApiContext apiContext, Endpoint endpoint,
                           List<ServiceInstance> records) {
    if (endpoint instanceof HttpEndpoint) {
      HttpEndpoint httpEndpoint = (HttpEndpoint) endpoint;
      HttpRpcRequest httpRpcRequest =
              HttpRpcRequest.create(apiContext.nextRpcId(), httpEndpoint.name());
      httpRpcRequest.setPath(httpEndpoint.path());
      httpRpcRequest.setHttpMethod(httpEndpoint.method());
      httpRpcRequest.addParams(apiContext.params());
//    httpRpcRequest.addHeaders(apiContext.headers());
      httpRpcRequest.addHeader("x-request-id", httpRpcRequest.id());
      httpRpcRequest.setBody(apiContext.body());
      List<ServiceInstance> instances = records.stream()
              .filter(r -> httpEndpoint.service().equalsIgnoreCase(r.name()))
              .collect(Collectors.toList());
      if (instances.isEmpty()) {
        Helper.logFailed(LOGGER, apiContext.id(),
                         this.getClass().getSimpleName(),
                         "Service not found, endpoint:" + endpoint.name());
        throw SystemException.create(DefaultErrorCode.SERVICE_UNAVAILABLE)
                .set("details", "Service not found, endpoint:" + endpoint.name());
      }
      ServiceInstance instance = instances.get(0);
      httpRpcRequest.setServerId(instance.id());
      httpRpcRequest.setHost(instance.record().getLocation().getString("host"));
      httpRpcRequest.setPort(instance.record().getLocation().getInteger("port"));
      return httpRpcRequest;
    }
    return null;
  }
}
