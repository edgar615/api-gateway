package com.edgar.direwolves.cmd;

import com.edgar.direwolves.core.cmd.ApiCmd;
import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.ApiDiscovery;
import com.edgar.direwolves.verticle.ApiDefinitionRegistry;
import com.edgar.util.base.Randoms;
import com.edgar.util.validation.ValidationException;
import io.vertx.core.Vertx;
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
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Edgar on 2017/1/19.
 *
 * @author Edgar  Date 2017/1/19
 */
@RunWith(VertxUnitRunner.class)
public class DeleteApiCmdTest {

  ApiDiscovery discovery;

  ApiCmd cmd;

  String namespace;
  Vertx vertx;

  @Before
  public void setUp() {
    namespace = UUID.randomUUID().toString();
    vertx = Vertx.vertx();
    discovery = ApiDiscovery.create(vertx, namespace);
    cmd = new DeleteApiCmdFactory().create(vertx, new JsonObject());

    AddApiCmd addApiCmd = new AddApiCmd(vertx);
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

    AtomicBoolean check1 = new AtomicBoolean();
    addApiCmd.handle(new JsonObject().put("namespace", namespace).put("data", jsonObject.encode()))
            .setHandler(ar -> {
              if (ar.succeeded()) {
                check1.set(true);
              } else {
                ar.cause().printStackTrace();
              }
            });
    Awaitility.await().until(() -> check1.get());

     jsonObject = new JsonObject()
            .put("name", "get_device")
            .put("method", "GET")
            .put("path", "/devices");
    endpoints = new JsonArray()
            .add(new JsonObject().put("type", "http")
                         .put("name", "get_device")
                         .put("service", "device")
                         .put("method", "GET")
                         .put("path", "/devices"));
    jsonObject.put("endpoints", endpoints);

    AtomicBoolean check2 = new AtomicBoolean();
    addApiCmd.handle(new JsonObject().put("namespace", namespace).put("data", jsonObject.encode()))
            .setHandler(ar -> {
              if (ar.succeeded()) {
                check2.set(true);
              } else {
                ar.cause().printStackTrace();
              }
            });
    Awaitility.await().until(() -> check2.get());
  }

  @Test
  public void testMissNameShouldThrowValidationException(TestContext testContext) {
    JsonObject jsonObject = new JsonObject();

    Async async = testContext.async();
    cmd.handle(jsonObject)
            .setHandler(ar -> {
              if (ar.succeeded()) {
                testContext.fail();
              } else {
                testContext.assertTrue(ar.cause() instanceof ValidationException);
                async.complete();
              }
            });
  }

  @Test
  public void testDeleteApiByFullname(TestContext testContext) {
    AtomicBoolean check1 = new AtomicBoolean();
    discovery.getDefinitions(new JsonObject(), ar -> {
      if (ar.failed()) {
        testContext.fail();
        return;
      }
      testContext.assertEquals(2, ar.result().size());
      check1.set(true);
    });
    Awaitility.await().until(() -> check1.get());

    AtomicBoolean check2 = new AtomicBoolean();
    JsonObject jsonObject = new JsonObject()
            .put("namespace", namespace)
            .put("name", "add_device");

    cmd.handle(jsonObject)
            .setHandler(ar -> {
              if (ar.succeeded()) {
                check2.set(true);
              } else {
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
  public void testDeleteApiByUndefinedNameShouldSuccess(TestContext testContext) {
    AtomicBoolean check1 = new AtomicBoolean();
    discovery.getDefinitions(new JsonObject(), ar -> {
      if (ar.failed()) {
        testContext.fail();
        return;
      }
      testContext.assertEquals(2, ar.result().size());
      check1.set(true);
    });
    Awaitility.await().until(() -> check1.get());

    AtomicBoolean check2 = new AtomicBoolean();
    JsonObject jsonObject = new JsonObject()
            .put("namespace", namespace)
            .put("name", UUID.randomUUID().toString());

    cmd.handle(jsonObject)
            .setHandler(ar -> {
              if (ar.succeeded()) {
                check2.set(true);
              } else {
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
      testContext.assertEquals(2, ar.result().size());
      check3.set(true);
    });
    Awaitility.await().until(() -> check3.get());
  }

  @Test
  public void testDeleteApiByWildcard(TestContext testContext) {
    AtomicBoolean check1 = new AtomicBoolean();
    discovery.getDefinitions(new JsonObject(), ar -> {
      if (ar.failed()) {
        testContext.fail();
        return;
      }
      testContext.assertEquals(2, ar.result().size());
      check1.set(true);
    });
    Awaitility.await().until(() -> check1.get());

    AtomicBoolean check2 = new AtomicBoolean();
    JsonObject jsonObject = new JsonObject()
            .put("namespace", namespace)
            .put("name", "*device");

    cmd.handle(jsonObject)
            .setHandler(ar -> {
              if (ar.succeeded()) {
                check2.set(true);
              } else {
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
      testContext.assertEquals(0, ar.result().size());
      check3.set(true);
    });
    Awaitility.await().until(() -> check3.get());
  }

}
