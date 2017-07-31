package com.edgar.direwolves.loadbalance;

import io.vertx.core.Vertx;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpEndpoint;
import org.awaitility.Awaitility;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Edgar on 2017/7/31.
 *
 * @author Edgar  Date 2017/7/31
 */
public class ServiceProviderTest {

  private Vertx vertx;

  private ServiceCache serviceCache;

  private String service;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
    ServiceDiscovery discovery = ServiceDiscovery.create(vertx);
    service = UUID.randomUUID().toString();
    AtomicInteger count = new AtomicInteger();
    for (int i = 0; i < 10; i++) {
      Record record = HttpEndpoint.createRecord(service, "localhost", 8081 + i, "/");
      discovery.publish(record, ar -> {
        count.incrementAndGet();
      });
    }
    Awaitility.await().until(() -> count.get());

    serviceCache = ServiceCache.create(vertx, discovery);
    try {
      TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testLoadBalance() {
    ServiceProvider serviceProvider = new ServiceProviderImpl(serviceCache, service);

    AtomicBoolean check = new AtomicBoolean();
    serviceProvider.choose(ar -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
        return;
      }
      System.out.println(ar.result().toJson());
      check.set(true);
    });
    Awaitility.await().until(() -> check.get());
  }

}
