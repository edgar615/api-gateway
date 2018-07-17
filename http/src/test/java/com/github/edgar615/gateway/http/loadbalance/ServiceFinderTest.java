package com.github.edgar615.gateway.http.loadbalance;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpEndpoint;
import org.awaitility.Awaitility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Edgar on 2017/8/1.
 *
 * @author Edgar  Date 2017/8/1
 */
@RunWith(VertxUnitRunner.class)
public class ServiceFinderTest {
  private Vertx vertx;

  private ServiceFinder serviceFinder;

  private ServiceDiscovery discovery;

  @Before
  public void setUp(TestContext testContext) {
    vertx = Vertx.vertx();
    discovery = ServiceDiscovery.create(vertx);
  }

  @Test
  public void testLoadAllServiceWhenCacheConstruct(TestContext testContext) {
    serviceFinder = ServiceFinder.create(vertx, discovery);

    AtomicInteger seq = new AtomicInteger();
    addService(seq);

    Awaitility.await().until(() -> seq.get() == 4);

    AtomicBoolean check = new AtomicBoolean();
    serviceFinder.getRecords(r -> "test".equals(r.getName()), ar -> {
      if (ar.succeeded()) {
        testContext.assertEquals(3, ar.result().size());
        check.set(true);
      }
    });

    Awaitility.await().until(() -> check.get());
  }

  @Test
  public void testAddServiceAfterCacheConstruct(TestContext testContext) {
    serviceFinder = ServiceFinder.create(vertx, discovery);

    AtomicInteger seq = new AtomicInteger();
    addService(seq);

    Awaitility.await().until(() -> seq.get() == 4);

    AtomicBoolean check = new AtomicBoolean();
    serviceFinder.getRecords(r -> "random".equals(r.getName()), ar -> {
      if (ar.succeeded()) {
        testContext.assertEquals(1, ar.result().size());
        check.set(true);
      }
    });

    Awaitility.await().until(() -> check.get());

    AtomicBoolean check2 = new AtomicBoolean();
    discovery.publish(HttpEndpoint.createRecord("random", "localhost", 8085, "/"),
                      ar -> check2.set(true));
    Awaitility.await().until(() -> check2.get());

    AtomicBoolean check3 = new AtomicBoolean();
    serviceFinder.getRecords(r -> "random".equals(r.getName()), ar -> {
      if (ar.succeeded()) {
        testContext.assertEquals(2, ar.result().size());
        check3.set(true);
      }
    });

    Awaitility.await().until(() -> check3.get());
  }

  private void addService(AtomicInteger seq) {
    discovery.publish(HttpEndpoint.createRecord("test", "localhost", 8081, "/"),
                      ar -> seq.incrementAndGet());
    discovery.publish(HttpEndpoint.createRecord("test", "localhost", 8082, "/"),
                      ar -> seq.incrementAndGet());
    discovery.publish(HttpEndpoint.createRecord("test", "localhost", 8083, "/"),
                      ar -> seq.incrementAndGet());
    discovery.publish(HttpEndpoint.createRecord("random", "localhost", 8084, "/"),
                      ar -> seq.incrementAndGet());
  }

}
