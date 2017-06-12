package com.edgar.direwolves.filter;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import com.edgar.direwolves.core.definition.Endpoint;
import com.edgar.direwolves.core.definition.HttpEndpoint;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.rpc.RpcRequest;
import com.edgar.direwolves.core.rpc.http.HttpRpcRequest;
import com.edgar.direwolves.core.utils.Helper;
import com.edgar.service.discovery.MoreServiceDiscovery;
import com.edgar.service.discovery.MoreServiceDiscoveryOptions;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.vertx.task.Task;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.spi.ServiceImporter;
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

  private final String CONSUL_PREFIX = "consul://";

  private final String ZOOKEEPER_PREFIX = "zookeeper://";

  private final String consulImportClass =
          "io.vertx.servicediscovery.consul.ConsulServiceImporter";

  private final String zookeeperImportClass =
          "com.edgar.service.discovery.zookeeper.ZookeeperServiceImporter";

  private final MoreServiceDiscovery discovery;

  private final Vertx vertx;

  ServiceDiscoveryFilter(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    MoreServiceDiscoveryOptions options = new MoreServiceDiscoveryOptions();
    this.discovery = MoreServiceDiscovery.create(vertx, options);
    String serviceDiscovery = config.getString("service.discovery");
    if (Strings.isNullOrEmpty(serviceDiscovery)) {
      throw SystemException.create(DefaultErrorCode.INVALID_ARGS)
              .set("details", "Config : service.discovery cannot be null");
    }
    if (serviceDiscovery.startsWith(CONSUL_PREFIX)) {
      registerConsul(serviceDiscovery, config);
    } else if (serviceDiscovery.startsWith(ZOOKEEPER_PREFIX)) {
      registerZookeeper(serviceDiscovery, config);
    } else {
      throw SystemException.create(DefaultErrorCode.INVALID_ARGS)
              .set("details", "Config : service.discovery:" + serviceDiscovery + " unsupported");
    }
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
    discovery.queryForInstance(service, ar -> {
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


  private void registerZookeeper(String serviceDiscovery, JsonObject config) {
    String address = serviceDiscovery.substring(ZOOKEEPER_PREFIX.length());
    JsonObject zkConfig = new JsonObject()
            .put("zookeeper.connect", address);
    if (config.containsKey("zookeeper.retry.times")) {
      zkConfig.put("zookeeper.retry.times", config.getValue("zookeeper.retry.times"));
    }
    if (config.containsKey("zookeeper.retry.sleep")) {
      zkConfig.put("zookeeper.retry.sleep", config.getValue("zookeeper.retry.sleep"));
    }
    if (config.containsKey("zookeeper.path")) {
      zkConfig.put("zookeeper.path", config.getValue("zookeeper.path"));
    }
    try {
      ServiceImporter serviceImporter =
              (ServiceImporter) Class.forName(zookeeperImportClass).newInstance();
      discovery.discovery()
              .registerServiceImporter(serviceImporter, zkConfig,
                                       Future.<Void>future().completer());
    } catch (Exception e) {
      throw SystemException.wrap(DefaultErrorCode.UNKOWN, e)
              .set("details", e.getMessage());
    }
  }

  private void registerConsul(String serviceDiscovery, JsonObject config) {
    String address = serviceDiscovery.substring(CONSUL_PREFIX.length());
    Iterable<String> iterable = Splitter.on(":").split(address);
    String host = Iterables.get(iterable, 0);
    int port = Integer.parseInt(Iterables.get(iterable, 1));
    Integer scanPeriod = config.getInteger("service.discovery.scan-period", 2000);
    try {
      ServiceImporter serviceImporter =
              (ServiceImporter) Class.forName(consulImportClass).newInstance();
      discovery.discovery()
              .registerServiceImporter(serviceImporter, new JsonObject()
                                               .put("host", host)
                                               .put("port", port)
                                               .put("scan-period", scanPeriod),
                                       Future.<Void>future().completer());
    } catch (Exception e) {
      throw SystemException.wrap(DefaultErrorCode.UNKOWN, e);
    }
  }
}
