package com.edgar.direwolves.service;

import static org.awaitility.Awaitility.await;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.verticle.MockConsulHttpVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.servicediscovery.Record;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Edgar on 2016/10/13.
 *
 * @author Edgar  Date 2016/10/13
 */
@RunWith(VertxUnitRunner.class)
public class ServiceDiscoveryVerticleTest {

  Vertx vertx;

  MockConsulHttpVerticle mockConsulHttpVerticle;

  @Before
  public void testSetUp(TestContext testContext) {
    vertx = Vertx.vertx();
    mockConsulHttpVerticle = new MockConsulHttpVerticle();
    vertx.deployVerticle(mockConsulHttpVerticle, testContext.asyncAssertSuccess());
    JsonObject config = new JsonObject()
            .put("service.discovery", "consul://localhost:5601");
    JsonObject strategy = new JsonObject();
    config.put("service.discovery.select-strategy", strategy);
    vertx.deployVerticle(ServiceDiscoveryVerticle.class.getName(),
                         new DeploymentOptions().setConfig(config),
                         testContext.asyncAssertSuccess());
  }

  @After
  public void tearDown(TestContext testContext) {
    vertx.close();
//    vertx.close(testContext.asyncAssertSuccess());
  }

  @Test
  public void testRoundRobin(TestContext context) {
    add2Servers();
    try {
      TimeUnit.SECONDS.sleep(3);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    Multimap<Integer, Record> group = ArrayListMultimap.create();
    for (int i = 0; i < 100; i ++) {
      vertx.eventBus().<JsonObject>send(ServiceDiscoveryVerticle.ADDRESS, "device", ar -> {
        if (ar.succeeded()) {
          JsonObject jsonObject = ar.result().body();
          Record record = new Record(jsonObject);
//        context.assertEquals("OK", jsonObject.getString("response"));
          int port = record.getLocation().getInteger("port");
          System.out.println(record.getName());
          group.put(port, record);
        } else {
          System.out.println(ar.cause());
          context.fail();
        }
      });
    }

    await().until(() -> group.size() == 100);
    Assert.assertEquals(50, group.get(32769).size());
    Assert.assertEquals(50, group.get(32770).size());

  }

  @Test
  public void testFailed(TestContext context) {
    add2Servers();
    try {
      TimeUnit.SECONDS.sleep(3);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    AtomicBoolean failed = new AtomicBoolean(false);
    vertx.eventBus().<JsonObject>send(ServiceDiscoveryVerticle.ADDRESS, "user", ar -> {
      if (ar.succeeded()) {
        context.fail();
      } else {
        System.out.println(ar.cause());
        failed.set(true);
      }
    });

    await().until(() -> failed.get());

  }

  private void add2Servers() {
    mockConsulHttpVerticle.addService(new JsonObject()
                                              .put("Node", "u221")
                                              .put("Address", "10.4.7.221")
                                              .put("ServiceID", "u221:device:8080")
                                              .put("ServiceName", "device")
                                              .put("ServiceTags", new JsonArray())
                                              .put("ServicePort", 32769));
    mockConsulHttpVerticle.addService((new JsonObject()
            .put("Node", "u222")
            .put("Address", "10.4.7.222")
            .put("ServiceID", "u222:device:8080")
            .put("ServiceName", "device")
            .put("ServiceTags", new JsonArray())
            .put("ServicePort", 32770)));
  }
}
