package com.edgar.direwolves.http.filter;

import com.edgar.direwolves.core.definition.HttpEndpoint;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.rpc.RpcRequest;
import com.edgar.direwolves.core.utils.Log;
import com.edgar.direwolves.http.SdHttpEndpoint;
import com.edgar.direwolves.http.SdHttpRequest;
import com.edgar.direwolves.http.loadbalance.LoadBalance;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.vertx.task.Task;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * HTTP类型的endpoint需要经过这个Filter进行服务发现，找到对应的服务地址，并转换为RpcRequest.
 *
 * @author Edgar  Date 2016/11/18
 */
@Deprecated
public class ServiceDiscoveryFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceDiscoveryFilter.class);

  private final Vertx vertx;

  private final LoadBalance loadBalance;

  private JsonObject config;

  ServiceDiscoveryFilter(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    this.config = config.getJsonObject("service.discovery", new JsonObject());
    loadBalance = LoadBalance.create(vertx, this.config);
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
                    .filter(e -> e instanceof HttpEndpoint)
                    .map(e -> ((SdHttpEndpoint) e).service())
                    .distinct()
                    .map(s -> serviceFuture(apiContext.id(), s))
                    .collect(Collectors.toList());


    Task.par(futures)
            .andThen(records -> {
              apiContext.apiDefinition().endpoints().stream()
                      .filter(e -> e instanceof HttpEndpoint)
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
      throw SystemException.create(DefaultErrorCode.SERVICE_UNAVAILABLE)
              .set("details", "Service not found, endpoint:" + endpoint.name());
    }
    Record instance = instances.get(0);
    httpRpcRequest.setRecord(new Record(instance));
    return httpRpcRequest;
  }

}
