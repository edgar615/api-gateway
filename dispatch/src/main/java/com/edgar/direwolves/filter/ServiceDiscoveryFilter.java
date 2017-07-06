package com.edgar.direwolves.filter;

import com.edgar.direwolves.core.definition.Endpoint;
import com.edgar.direwolves.core.definition.HttpEndpoint;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.rpc.RpcRequest;
import com.edgar.direwolves.core.rpc.http.HttpRpcRequest;
import com.edgar.direwolves.core.utils.Helper;
import com.edgar.servicediscovery.ServiceProviderRegistry;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.vertx.JsonUtils;
import com.edgar.util.vertx.task.Task;
import io.vertx.circuitbreaker.CircuitBreakerState;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * HTTP类型的endpoint需要经过这个Filter进行服务发现，找到对应的服务地址，并转换为RpcRequest.
 *
 * @author Edgar  Date 2016/11/18
 */
public class ServiceDiscoveryFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceDiscoveryFilter.class);

  private final String configPrefix = "service.discovery.";

  private final Vertx vertx;

  private final ServiceProviderRegistry providerRegistry;

  private final CircuitbreakerPredicate circuitbreakerPredicate;

  private JsonObject config;

  ServiceDiscoveryFilter(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    this.config = JsonUtils.extractByPrefix(config, configPrefix, true);
    providerRegistry = ServiceProviderRegistry.create(vertx, config);
    circuitbreakerPredicate = new CircuitbreakerPredicate(vertx);
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
    List<Future<Record>> futures =
            apiContext.apiDefinition().endpoints().stream()
                    .filter(e -> e instanceof HttpEndpoint)
                    .map(e -> ((HttpEndpoint) e).service())
                    .distinct()
                    .map(s -> serviceFuture(s))
                    .collect(Collectors.toList());


    Task.par(futures)
            .andThen(records -> {
              apiContext.apiDefinition().endpoints().stream()
                      .filter(e -> e instanceof HttpEndpoint)
                      .map(e -> toRpc(apiContext, e, records))
                      .forEach(req -> apiContext.addRequest(req));
            })
            .andThen(records -> {
              completeFuture.complete(apiContext);
            })
            .onFailure(throwable -> completeFuture.fail(throwable));

  }

  private Future<Record> serviceFuture(String service) {
    Future<Record> future = Future.future();
    providerRegistry.get(service)
            .getInstance(r -> circuitbreakerPredicate.test(r), ar -> {
              if (ar.failed()) {
                future.fail(SystemException.create(DefaultErrorCode.SERVICE_UNAVAILABLE)
                                    .set("details", "Service not found: " + service));
                return;
              }
              Record record = ar.result();
              if (record == null) {
                future.fail(SystemException.create(DefaultErrorCode.SERVICE_UNAVAILABLE)
                                    .set("details", "Service not found: " + service));
                return;
              }
              future.complete(record);
            });
    return future;

  }

  private RpcRequest toRpc(ApiContext apiContext, Endpoint endpoint,
                           List<Record> records) {
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
      List<Record> instances = records.stream()
              .filter(r -> httpEndpoint.service().equalsIgnoreCase(r.getName()))
              .collect(Collectors.toList());
      if (instances.isEmpty()) {
        Helper.logFailed(LOGGER, apiContext.id(),
                         this.getClass().getSimpleName(),
                         "Service not found, endpoint:" + endpoint.name());
        throw SystemException.create(DefaultErrorCode.SERVICE_UNAVAILABLE)
                .set("details", "Service not found, endpoint:" + endpoint.name());
      }
      Record instance = instances.get(0);
      httpRpcRequest.setServerId(instance.getRegistration());
      httpRpcRequest.setHost(instance.getLocation().getString("host"));
      httpRpcRequest.setPort(instance.getLocation().getInteger("port"));
      return httpRpcRequest;
    }
    return null;
  }

  private class CircuitbreakerPredicate implements Predicate<Record> {

    private final Map<String, CircuitBreakerRegistry> breakerMap;

    private final Vertx vertx;

    public CircuitbreakerPredicate(Vertx vertx) {
      this.vertx = vertx;
      this.breakerMap = vertx.sharedData().getLocalMap("circuit.breaker.registry");
    }

    @Override
    public boolean test(Record record) {
      if (!breakerMap.containsKey(record.getRegistration())) {
        return true;
      }
      CircuitBreakerRegistry registry = breakerMap.get(record.getRegistration());
      return registry.get().state() != CircuitBreakerState.OPEN;
    }
  }
}
