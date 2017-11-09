package com.github.edgar615.servicediscovery.verticle;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.servicediscovery.ServiceDiscovery;
import org.awaitility.Awaitility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Edgar on 2017/11/9.
 *
 * @author Edgar  Date 2017/11/9
 */
@RunWith(VertxUnitRunner.class)
public class ConsulServiceDiscoveryVerticleTest {

  MockConsulHttpVerticle mockConsulHttpVerticle;

  private Vertx vertx;

  private ServiceDiscovery serviceDiscovery;

  private int port;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
    serviceDiscovery = ServiceDiscovery.create(vertx);

    port = new Random().nextInt(10000);

    mockConsulHttpVerticle = new MockConsulHttpVerticle();
    JsonObject consul = new JsonObject()
            .put("port", port);
    AtomicBoolean check = new AtomicBoolean();
    vertx.deployVerticle(mockConsulHttpVerticle, new DeploymentOptions().setConfig(consul),
                         ar -> {
                           check.set(true);
                         });
  }

  @Test
  public void testPublish(TestContext testContext) {
    add2Servers();
    JsonObject consul = new JsonObject()
            .put("host", "127.0.0.1")
            .put("port", port);
    JsonObject config = new JsonObject()
            .put("consul", consul);
    vertx.deployVerticle(ConsulServiceDiscoveryVerticle.class.getName(), new DeploymentOptions()
            .setConfig(config));
    try {
      TimeUnit.SECONDS.sleep(3);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    AtomicBoolean check = new AtomicBoolean();
    serviceDiscovery.getRecords(r -> true, ar -> {
      if (ar.succeeded()) {
        testContext.assertEquals(2, ar.result().size());
        check.set(true);
      } else {
        testContext.fail();
      }
    });
    Awaitility.await().until(() -> check.get());

    AtomicBoolean check2 = new AtomicBoolean();
    serviceDiscovery.getRecords(new JsonObject().put("name", "device"), ar -> {
      if (ar.succeeded()) {
        testContext.assertEquals(1, ar.result().size());
        check2.set(true);
      } else {
        testContext.fail();
      }
    });
    Awaitility.await().until(() -> check2.get());
  }

  private void add2Servers() {
    mockConsulHttpVerticle.addService(new JsonObject()
                                              .put("ID", UUID.randomUUID().toString())
                                              .put("Node", "u221")
                                              .put("Address", "localhost")
                                              .put("ServiceID", "u221:device:8080")
                                              .put("ServiceName", "device")
                                              .put("ServiceTags", new JsonArray())
                                              .put("ServicePort", 8080));
    mockConsulHttpVerticle.addService((new JsonObject()
            .put("ID", UUID.randomUUID().toString())
            .put("Node", "u222")
            .put("Address", "localhost")
            .put("ServiceID", "u222:device:8080")
            .put("ServiceName", "user")
            .put("ServiceTags", new JsonArray())
            .put("ServicePort", 8081)));
    try {
      TimeUnit.SECONDS.sleep(3);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
