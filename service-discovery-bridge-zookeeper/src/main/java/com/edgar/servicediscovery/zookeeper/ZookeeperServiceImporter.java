package com.edgar.servicediscovery.zookeeper;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.spi.ServiceImporter;
import io.vertx.servicediscovery.spi.ServicePublisher;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceProvider;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * 参照Consul实现的服务发现.
 *
 * @author Edgar  Date 2017/2/6
 */
public class ZookeeperServiceImporter implements ServiceImporter, TreeCacheListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperServiceImporter.class);

  private final ConcurrentMap<String, ServiceProvider<String>> providers =
          new ConcurrentHashMap<>();

  private final List<ImportedZookeeperService> imports = new ArrayList<>();

  /**
   * Zookeeper的连接串.
   */
  private String zkConnect;

  /**
   * 服务注册在Zookeeper中的目录.
   */
  private String basePath;

  /**
   * 重试的间隔时间.
   */
  private int sleepMsBetweenRetries;

  /**
   * 重试次数.
   */
  private int retryTimes = 5;

  private Vertx vertx;

  private ServicePublisher publisher;

  private CuratorFramework client;

  private ServiceDiscovery<String> serviceDiscovery;

  private TreeCache cache;

  private volatile boolean started;

  private synchronized ImportedZookeeperService getImportedServiceById(String id) {
    for (ImportedZookeeperService svc : imports) {
      if (svc.id().equals(id)) {
        return svc;
      }
    }
    return null;
  }

  private synchronized void retrieveIndividualServices(Future<Void> completed) {
    List<ServiceInstance<String>> instances = new ArrayList<>();
    try {
      Collection<String> names = serviceDiscovery.queryForNames();
      for (String name : names) {
        instances.addAll(serviceDiscovery.queryForInstances(name));
      }
    } catch (KeeperException.NoNodeException e) {
      // no services
      // Continue
    } catch (Exception e) {
      if (completed != null) {
        completed.fail(e);
      } else {
        LOGGER.error("Unable to retrieve service instances from Zookeeper", e);
        return;
      }
    }
    Future<List<ImportedZookeeperService>> future = Future.future();
    importService(instances, future);
    future.setHandler(ar -> {
      if (ar.failed()) {
        //completed.fail(ar.cause());
        unregisterAllServices(completed);
      } else {
        List<ImportedZookeeperService> services = future.result();
        List<String> retrievedIds =
                services.stream().map(ImportedZookeeperService::id).collect(Collectors.toList());
        List<String> existingIds =
                imports.stream().map(ImportedZookeeperService::id).collect(Collectors.toList());

        LOGGER.trace("Imported services: " + existingIds + ", Retrieved services form Zookeeper: "
                     + retrievedIds);

        services.forEach(svc -> {
          String id = svc.id();

          if (!existingIds.contains(id)) {
            LOGGER.info("Imported service: " + id);
            imports.add(svc);
          }
        });

        //使用foreach删除会出现ConcurrentModificationException
        Iterator<ImportedZookeeperService> iterator = imports.iterator();
        while (iterator.hasNext()) {
          ImportedZookeeperService svc = iterator.next();
          if (!retrievedIds.contains(svc.id())) {
            LOGGER.info("Unregistering " + svc.id());
            iterator.remove();
            svc.unregister(publisher, null);
          }
        }

        completed.complete();
      }
    });
  }

  private synchronized void unregisterAllServices(Future<Void> completed) {
    List<Future> list = new ArrayList<>();

    imports.forEach(svc -> {
      Future<Void> unreg = Future.future();
      svc.unregister(publisher, unreg);
      list.add(unreg);
    });

    CompositeFuture.all(list).setHandler(x -> {
      if (x.failed()) {
        completed.fail(x.cause());
      } else {
        completed.complete();
      }
    });
  }

  @Override
  public void start(Vertx vertx, ServicePublisher publisher, JsonObject configuration,
                    Future<Void> future) {
    this.vertx = vertx;
    this.publisher = publisher;
    this.zkConnect = configuration.getString("zookeeper.connect", "localhost:2181");
    this.basePath = configuration.getString("zookeeper.path", "/services");
    this.sleepMsBetweenRetries = configuration.getInteger("zookeeper.retry.sleep", 1000);
    this.retryTimes = configuration.getInteger("zookeeper.retry.times", 5);
    // When the bridge is configured, ready and has imported / exported the initial services, it
    // must complete the given Future. If the bridge starts method is blocking, it must use an
    // executeBlocking construct, and complete the given future object
    vertx.<Void>executeBlocking(
            f -> {
              try {
                client = CuratorFrameworkFactory.newClient(zkConnect,
                                                           new RetryNTimes(retryTimes,
                                                                           sleepMsBetweenRetries));
                client.start();

                serviceDiscovery =
                        ServiceDiscoveryBuilder.builder(String.class)
                                .basePath(basePath)
                                .watchInstances(true)
                                .client(client).build();

                serviceDiscovery.start();

                cache = TreeCache.newBuilder(client, basePath).build();
                cache.start();
                cache.getListenable().addListener(this);

                f.complete();
              } catch (Exception e) {
                future.fail(e);
              }
            },
            ar -> {
              if (ar.failed()) {
                future.fail(ar.cause());
              } else {
                Future<Void> f = Future.future();
                f.setHandler(x -> {
                  if (x.failed()) {
                    future.fail(x.cause());
                  } else {
                    started = true;
                    future.complete();
                  }
                });
                retrieveIndividualServices(f);
              }
            }
    );
  }

  @Override
  public void close(Handler<Void> closeHandler) {
    Future<Void> done = Future.future();
    //删除所有服务实例
    unregisterAllServices(done);

    done.setHandler(v -> {
      try {
        if (cache != null) {
          CloseableUtils.closeQuietly(cache);
        }
        if (serviceDiscovery != null) {
          CloseableUtils.closeQuietly(serviceDiscovery);
        }
        if (client != null) {
          CloseableUtils.closeQuietly(client);
        }
      } catch (Exception e) {
        // Ignore them
      }
      closeHandler.handle(null);
    });
  }

  @Override
  public void childEvent(CuratorFramework curatorFramework,
                         TreeCacheEvent treeCacheEvent) throws Exception {
    if (started) {
      retrieveIndividualServices(Future.future());
    }
  }

  /**
   * 注册服务，如果对应的服务实例在缓存列表中不存在，注册服务实例
   *
   * @param instances 服务实例列表
   * @param future
   */
  private void importService(List<ServiceInstance<String>> instances,
                             Future<List<ImportedZookeeperService>> future) {
    if (instances.isEmpty()) {
      future.fail("no service with the given name");
    } else {
      List<Future> registrations = new ArrayList<>();
      List<ImportedZookeeperService> importedServices = new ArrayList<>();
      for (ServiceInstance<String> instance : instances) {
        Future<Void> registration = Future.future();
        String id = instance.getId();
        String name = instance.getName();
        Record record = createRecord(instance);
        ImportedZookeeperService imported = getImportedServiceById(id);
        if (imported != null) {
          importedServices.add(imported);
          registration.complete();
        } else {
          LOGGER.info("Importing service " + record.getName() + " (" + id + ")"
                      + " from zookeeper");
          ImportedZookeeperService service = new ImportedZookeeperService(name, id, record);
          //注册服务
          service.register(publisher, Future.<ImportedZookeeperService>future().setHandler(res -> {
            if (res.succeeded()) {
              importedServices.add(res.result());
              registration.complete();
            } else {
              registration.fail(res.cause());
            }
          }));
        }
        registrations.add(registration);
      }
      CompositeFuture.all(registrations).setHandler(ar -> {
        if (ar.succeeded()) {
          future.complete(importedServices);
        } else {
          future.fail(ar.cause());
        }
      });
    }
  }

  private Record createRecord(ServiceInstance<String> instance) {
    Record record = new Record();
    record.setName(instance.getName());
    String payload = instance.getPayload();
    JsonObject meta = new JsonObject(payload);
    if (meta.containsKey("version")) {
      record.getMetadata().put("version", meta.getString("version"));
    }
    record.getMetadata().put("zookeeper-payload", payload);
    record.getMetadata().put("zookeeper-service-type", instance.getServiceType().toString());
    record.getMetadata().put("zookeeper-host", instance.getAddress());
    record.getMetadata().put("zookeeper-registration-time",
                             instance.getRegistrationTimeUTC());
    record.getMetadata().put("zookeeper-port", instance.getPort());
    record.getMetadata().put("zookeeper-ssl-port", instance.getSslPort());
    record.getMetadata().put("zookeeper-id", instance.getId());

    record.setLocation(new JsonObject());
    if (instance.getUriSpec() != null) {
      String uri = instance.buildUriSpec();
      record.getLocation().put("endpoint", uri);
    } else {
      String uri = "http";
      if (instance.getSslPort() != null) {
        uri += "s://" + instance.getAddress() + ":" + instance.getSslPort();
      } else if (instance.getPort() != null) {
        uri += "s://" + instance.getAddress() + ":" + instance.getPort();
      } else {
        uri += "://" + instance.getAddress();
      }
      record.getLocation().put("endpoint", uri);
    }
    if (instance.getPort() != null) {
      record.getLocation().put("port", instance.getPort());
    }
    if (instance.getSslPort() != null) {
      record.getLocation().put("ssl-port", instance.getSslPort());
    }
    if (instance.getAddress() != null) {
      record.getLocation().put("host", instance.getAddress());
    }
    record.setType("http-endpoint");
//    record.setType(payload.getString("service-type", ServiceType.UNKNOWN));
    return record;
  }
}
