package com.edgar.direwolves.cmd;

import com.edgar.direwolves.core.cmd.ApiCmd;
import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.ApiDiscovery;
import com.edgar.direwolves.verticle.ApiDefinitionRegistry;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Edgar on 2017/1/19.
 *
 * @author Edgar  Date 2017/1/19
 */
@RunWith(VertxUnitRunner.class)
public class AddApiCmdTest {

  Vertx vertx;

  ApiDiscovery discovery;

  String namespace;
  ApiCmd cmd;
  @Before
  public void setUp() {
    namespace = UUID.randomUUID().toString();
    vertx = Vertx.vertx();
    discovery = ApiDiscovery.create(vertx, namespace);
    cmd = new AddApiCmdFactory().create(vertx, new JsonObject());
  }

  @Test
  public void testAddApiSuccess(TestContext testContext) {
    AtomicBoolean check1 = new AtomicBoolean();
    discovery.getDefinitions(new JsonObject(), ar -> {
      if (ar.failed()) {
        testContext.fail();
        return;
      }
      testContext.assertEquals(0, ar.result().size());
      check1.set(true);
    });
    Awaitility.await().until(() -> check1.get());

    JsonObject jsonObject = new JsonObject()
            .put("name", "add_device")
            .put("method", "POST")
            .put("path", "/devices");
    JsonArray endpoints = new JsonArray()
            .add(new JsonObject().put("type", "http")
                         .put("name", "add_device")
                         .put("service", "device")
                         .put("method", "POST")
                         .put("path", "/devices"));
    jsonObject.put("endpoints", endpoints);

    AtomicBoolean check2 = new AtomicBoolean();
    cmd.handle(new JsonObject().put("namespace", namespace).put("data", jsonObject))
            .setHandler(ar -> {
              if (ar.succeeded()) {
                testContext.assertEquals(1, ar.result().getInteger("result"));
                check2.set(true);
              } else {
                ar.cause().printStackTrace();
                testContext.fail();
              }
            });
    Awaitility.await().until(() -> check2.get());

    AtomicBoolean check3 = new AtomicBoolean();
    discovery.getDefinitions(new JsonObject(), ar -> {
      if (ar.failed()) {
        testContext.fail();
        return;
      }
      System.out.println(ar.result());
      testContext.assertEquals(1, ar.result().size());
      check3.set(true);
    });
    Awaitility.await().until(() -> check3.get());
  }

  @Test
  public void testAddApiWithPlugin(TestContext testContext) {
    AtomicBoolean check1 = new AtomicBoolean();
    discovery.getDefinitions(new JsonObject(), ar -> {
      if (ar.failed()) {
        testContext.fail();
        return;
      }
      testContext.assertEquals(0, ar.result().size());
      check1.set(true);
    });
    Awaitility.await().until(() -> check1.get());

    JsonObject jsonObject = new JsonObject()
            .put("name", "add_device")
            .put("method", "POST")
            .put("path", "/devices");
    JsonArray endpoints = new JsonArray()
            .add(new JsonObject().put("type", "http")
                         .put("name", "add_device")
                         .put("service", "device")
                         .put("method", "POST")
                         .put("path", "/devices"));
    jsonObject.put("endpoints", endpoints);
    jsonObject.put("authentication", true);

    AtomicBoolean check2 = new AtomicBoolean();
    cmd.handle(new JsonObject().put("namespace", namespace).put("data", jsonObject))
            .setHandler(ar -> {
              if (ar.succeeded()) {
                testContext.assertEquals(1, ar.result().getInteger("result"));
                check2.set(true);
              } else {
                ar.cause().printStackTrace();
                testContext.fail();
              }
            });
    Awaitility.await().until(() -> check2.get());

    AtomicBoolean check3 = new AtomicBoolean();
    discovery.getDefinitions(new JsonObject(), ar -> {
      if (ar.failed()) {
        testContext.fail();
        return;
      }
      System.out.println(ar.result());
      testContext.assertEquals(1, ar.result().size());
      ApiDefinition definition = ar.result().get(0);
      testContext.assertEquals(1, definition.plugins().size());
      check3.set(true);
    });
    Awaitility.await().until(() -> check3.get());


  }

}
