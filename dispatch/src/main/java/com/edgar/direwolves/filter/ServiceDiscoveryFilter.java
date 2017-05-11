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
import com.edgar.service.discovery.ServiceDiscovery;
import com.edgar.service.discovery.ServiceImporter;
import com.edgar.service.discovery.ServiceInstance;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
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

  private final String CONSUL_PREFIX = "consul://";

  private final String ZOOKEEPER_PREFIX = "zookeeper://";

  private final String consulImportClass =
          "com.edgar.service.discovery.consul.ConsulServiceImporter";

  private final String zookeeperImportClass =
          "com.edgar.service.discovery.zookeeper.ZookeeperServiceImporter";

  private final ServiceDiscovery discovery;

  private final Vertx vertx;

  ServiceDiscoveryFilter(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    this.discovery = ServiceDiscovery.create(vertx, config);
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
      ServiceInstance instance = discovery.getInstance(service);
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
      httpRpcRequest.setHost(instance.location().getString("host"));
      httpRpcRequest.setPort(instance.location().getInteger("port"));
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
      discovery
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
      discovery
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
