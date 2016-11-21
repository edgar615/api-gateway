package com.edgar.direwolves.filter.servicediscovery;

import com.edgar.direwolves.servicediscovery.RecordSelect;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.vertx.core.Future;
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

import static org.awaitility.Awaitility.await;

/**
 * Created by Edgar on 2016/10/12.
 *
 * @author Edgar  Date 2016/10/12
 */
@RunWith(VertxUnitRunner.class)
public class ConsulRecordSelectTest {

  Vertx vertx;

  MockConsulHttpVerticle mockConsulHttpVerticle;

  @Before
  public void setup(TestContext testContext) {
    vertx = Vertx.vertx();
    mockConsulHttpVerticle = new MockConsulHttpVerticle();
    vertx.deployVerticle(mockConsulHttpVerticle, testContext.asyncAssertSuccess());
  }

  @After
  public void tearDown(TestContext testContext) {
    mockConsulHttpVerticle.clear();
    vertx.close(testContext.asyncAssertSuccess());
    mockConsulHttpVerticle = null;
  }

  @Test
  public void testUnRegisterService(TestContext testContext) {
    add2Servers();
    RecordSelect recordSelect = RecordSelect.create();
    JsonObject config = new JsonObject()
        .put("service.discovery", "consul://localhost:5601");
    recordSelect.config(vertx, config);
    try {
      TimeUnit.SECONDS.sleep(3);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    Future<Record> future = recordSelect.select("user");
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        Record record = ar.result();
        testContext.assertNull(record);
      } else {
        testContext.fail();
      }
    });
  }

  @Test
  public void testDefaultStrategy(TestContext testContext) {
    add2Servers();
    RecordSelect recordSelect = RecordSelect.create();
    JsonObject config = new JsonObject()
        .put("service.discovery", "consul://localhost:5601");
    recordSelect.config(vertx, config);

    Multimap<Integer, Record> group = select100(recordSelect);
    await().until(() -> group.size() == 100);
    Assert.assertEquals(50, group.get(32769).size());
    Assert.assertEquals(50, group.get(32770).size());
  }

  @Test
  public void testRoundRobin(TestContext testContext) {
    add2Servers();
    RecordSelect recordSelect = RecordSelect.create();
    JsonObject config = new JsonObject()
        .put("service.discovery", "consul://localhost:5601");
    JsonObject strategy = new JsonObject();
    config.put("service.discovery.select-strategy", strategy);
    strategy.put("device", "round_robin");
    recordSelect.config(vertx, config);

    Multimap<Integer, Record> group = select100(recordSelect);
    await().until(() -> group.size() == 100);
    Assert.assertEquals(50, group.get(32769).size());
    Assert.assertEquals(50, group.get(32770).size());
  }

  @Test
  public void testRandom(TestContext testContext) {
    add2Servers();
    RecordSelect recordSelect = RecordSelect.create();
    JsonObject config = new JsonObject()
        .put("service.discovery", "consul://localhost:5601");
    JsonObject strategy = new JsonObject();
    config.put("service.discovery.select-strategy", strategy);
    strategy.put("device", "random");
    recordSelect.config(vertx, config);


    Multimap<Integer, Record> group =
        select100(recordSelect);
    await().until(() -> group.size() == 100);
    Assert.assertFalse(group.get(32769).size() == group.get(32770).size());
  }

  private Multimap<Integer, Record> select100(RecordSelect recordSelect) {
    try {
      TimeUnit.SECONDS.sleep(3);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    Multimap<Integer, Record> group = ArrayListMultimap.create();
    for (int i = 0; i < 100; i++) {
      Future<Record> future = recordSelect.select("device");
      future.setHandler(ar -> {
        if (ar.succeeded()) {
          Record record = ar.result();
          int port = record.getLocation().getInteger("port");
          synchronized (ConsulRecordSelectTest.class) {
            group.put(port, record);
          }
        } else {
          ar.cause().printStackTrace();
        }
      });
    }
    return group;
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
