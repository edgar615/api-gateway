package com.edgar.direwolves.discovery.consul;

import com.edgar.direwolves.discovery.ServiceDiscoveryInternal;
import com.edgar.direwolves.discovery.ServiceImporter;
import com.edgar.direwolves.discovery.ServiceInstance;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ConsulServiceImporter implements ServiceImporter {

  private final static Logger LOGGER = LoggerFactory.getLogger(ConsulServiceImporter.class);
  private final List<ImportedConsulService> imports = new ArrayList<>();
  private ServiceDiscoveryInternal serviceDiscovery;
  private HttpClient client;
  private String dc;
  private long scanTask = -1;
  private Vertx vertx;

  @Override
  public void start(Vertx vertx, ServiceDiscoveryInternal serviceDiscovery, JsonObject configuration, Future<Void> completion) {
    this.vertx = vertx;
    this.serviceDiscovery = serviceDiscovery;

    HttpClientOptions options = new HttpClientOptions(configuration);
    String host = configuration.getString("host", "localhost");
    int port = configuration.getInteger("port", 8500);

    options.setDefaultHost(host);
    options.setDefaultPort(port);

    dc = configuration.getString("dc");
    client = vertx.createHttpClient(options);

    Future<List<ImportedConsulService>> imports = Future.future();

    retrieveServicesFromConsul(imports);

    imports.setHandler(ar -> {
      if (ar.succeeded()) {
        Integer period = configuration.getInteger("scan-period", 2000);
        if (period != 0) {
          scanTask = vertx.setPeriodic(period, l -> {
            Future<List<ImportedConsulService>> future = Future.future();
            future.setHandler(ar2 -> {
              if (ar2.failed()) {
                LOGGER.warn("Consul importation has failed", ar.cause());
              }
            });
            retrieveServicesFromConsul(future);
          });
        }
        completion.complete();
      } else {
        completion.fail(ar.cause());
      }
    });

  }


  private Handler<Throwable> getErrorHandler(Future future) {
    return t -> {
      if (future != null) {
        if (!future.isComplete()) {
          future.fail(t);
        }
      } else {
        LOGGER.error("", t);
      }
    };
  }

  private void retrieveServicesFromConsul(Future<List<ImportedConsulService>> completed) {
    String path = "/v1/catalog/services";
    if (dc != null) {
      path += "?dc=" + dc;
    }

    Handler<Throwable> error = getErrorHandler(completed);

    client.get(path)
        .exceptionHandler(error)
        .handler(
            response -> response
                .exceptionHandler(error)
                .bodyHandler(buffer -> retrieveIndividualServices(buffer.toJsonObject(), completed)))
        .end();
  }

  private void retrieveIndividualServices(JsonObject jsonObject, Future<List<ImportedConsulService>> completed) {
    List<Future> futures = new ArrayList<>();
    jsonObject.fieldNames().forEach(name -> {

      Future<List<ImportedConsulService>> future = Future.future();
      Handler<Throwable> error = getErrorHandler(future);
      String path = "/v1/catalog/service/" + name;
      if (dc != null) {
        path += "?dc=" + dc;
      }

      client.get(path)
          .exceptionHandler(error)
          .handler(response -> {
            response.exceptionHandler(error)
                .bodyHandler(buffer -> {
                  importService(buffer.toJsonArray(), future);
                });
          })
          .end();

      futures.add(future);
    });

    CompositeFuture.all(futures).setHandler(ar -> {
      if (ar.failed()) {
        LOGGER.error("Fail to retrieve the services from consul", ar.cause());
      } else {
        List<ImportedConsulService> services =
            futures.stream().map(future -> ((Future<List<ImportedConsulService>>) future).result())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        List<String> retrievedIds = services.stream().map(ImportedConsulService::id).collect(Collectors.toList());

        synchronized (ConsulServiceImporter.this) {

          List<String> existingIds = imports.stream().map(ImportedConsulService::id).collect(Collectors.toList());

          LOGGER.trace("Imported services: " + existingIds + ", Retrieved services form Consul: " + retrievedIds);

          services.forEach(svc -> {
            String id = svc.id();

            if (!existingIds.contains(id)) {
              LOGGER.info("Imported service: " + id);
              imports.add(svc);
            }
          });

          imports.forEach(svc -> {
            if (!retrievedIds.contains(svc.id())) {
              LOGGER.info("Unregistering " + svc.id());
              imports.remove(svc);
              svc.unregister(serviceDiscovery);
            }
          });
        }
      }

      if (ar.succeeded()) {
        completed.complete();
      } else {
        completed.fail(ar.cause());
      }
    });
  }

  private void importService(JsonArray array, Future<List<ImportedConsulService>> future) {
    if (array.isEmpty()) {
      future.fail("no service with the given name");
    } else {
      List<ImportedConsulService> importedServices = new ArrayList<>();
      List<Future> registrations = new ArrayList<>();
      for (int i = 0; i < array.size(); i++) {
        Future<Void> registration = Future.future();

        JsonObject jsonObject = array.getJsonObject(i);
        String id = jsonObject.getString("ID", jsonObject.getString("ServiceID"));
        String name = jsonObject.getString("ServiceName");
        ServiceInstance instance = createInstance(jsonObject, name);

        // the id must be unique, so check if the service has already being imported
        ImportedConsulService imported = getImportedServiceById(id);
        if (imported != null) {
          importedServices.add(imported);
          registration.complete();
        } else {
          LOGGER.info("Importing service " + instance.name() + " (" + id + ")"
              + " from consul");
          ImportedConsulService service = new ImportedConsulService(name, id, instance);
          service.register(serviceDiscovery);
          importedServices.add(service);
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

  private ServiceInstance createInstance(JsonObject jsonObject, String name) {
    String address = jsonObject.getString("Address");

    JsonArray tags = jsonObject.getJsonArray("ServiceTags");
    if (tags == null) {
      tags = new JsonArray();
    }

    String path = jsonObject.getString("ServiceAddress");
    int port = jsonObject.getInteger("ServicePort");

    JsonObject metadata = jsonObject.copy();
    tags.stream().forEach(tag -> metadata.put((String) tag, true));

    String id = metadata.getString("ID", metadata.getString("ServiceID"));

    ServiceInstance instance = new ServiceInstance(id, name);
    //TODO 服务节点数据

    JsonObject location = new JsonObject();
    location.put("host", address);
    location.put("port", port);
    if (path != null) {
      location.put("path", path);
    }

//    record.setLocation(location);
    return instance;
  }

  private synchronized ImportedConsulService getImportedServiceById(String id) {
    for (ImportedConsulService svc : imports) {
      if (svc.id().equals(id)) {
        return svc;
      }
    }
    return null;
  }

  @Override
  public synchronized void close(Handler<Void> completionHandler) {
    if (scanTask != -1) {
      vertx.cancelTimer(scanTask);
    }
    // Remove all the services that has been imported
    List<Future> list = new ArrayList<>();
    imports.forEach(imported -> {
      Future<Void> fut = Future.future();
      fut.setHandler(ar -> {
        LOGGER.info("Unregistering " + imported.name());
        if (ar.succeeded()) {
          list.add(Future.succeededFuture());
        } else {
          list.add(Future.failedFuture(ar.cause()));
        }
      });
      imported.unregister(serviceDiscovery);
    });

    CompositeFuture.all(list).setHandler(ar -> {
      imports.clear();
      if (ar.succeeded()) {
        LOGGER.info("Successfully closed the service importer " + this);
      } else {
        LOGGER.error("A failure has been caught while stopping " + this, ar.cause());
      }
      if (completionHandler != null) {
        completionHandler.handle(null);
      }
    });
  }
}