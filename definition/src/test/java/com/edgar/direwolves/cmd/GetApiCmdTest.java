package com.edgar.direwolves.cmd;

import com.edgar.direvolves.plugin.authentication.AuthenticationPlugin;
import com.edgar.direwolves.core.cmd.ApiCmd;
import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.ApiDiscovery;
import com.edgar.util.validation.ValidationException;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.awaitility.Awaitility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Edgar on 2017/1/19.
 *
 * @author Edgar  Date 2017/1/19
 */
@RunWith(VertxUnitRunner.class)
public class GetApiCmdTest {

  ApiDiscovery discovery;

  ApiCmd cmd;

  String namespace;

  Vertx vertx;

  @Before
  public void setUp() {
    namespace = UUID.randomUUID().toString();
    vertx = Vertx.vertx();
    discovery = ApiDiscovery.create(vertx, namespace);
    cmd = new GetApiCmdFactory().create(vertx, new JsonObject());

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
    jsonObject.put("authentication", true);

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
  public void testGetApiByFullname(TestContext testContext) {
    JsonObject jsonObject = new JsonObject()
            .put("namespace", namespace)
            .put("name", "add_device");
    AtomicBoolean check1 = new AtomicBoolean();

    cmd.handle(jsonObject)
            .setHandler(ar -> {
              if (ar.succeeded()) {
                System.out.println(ar.result());
                ApiDefinition definition = ApiDefinition.fromJson(ar.result());
                testContext.assertEquals("add_device", definition.name());
                testContext.assertEquals(1, definition.plugins().size());
                testContext.assertNotNull(definition.plugin(
                        AuthenticationPlugin.class.getSimpleName()));
                check1.set(true);
              } else {
                testContext.fail();
              }
            });
    Awaitility.await().until(() -> check1.get());

  }


  @Test
  public void testGetApiByUndefinedNameShouldFailed(TestContext testContext) {
    JsonObject jsonObject = new JsonObject()
            .put("namespace", namespace)
            .put("name", "*device");
    AtomicBoolean check1 = new AtomicBoolean();

    cmd.handle(jsonObject)
            .setHandler(ar -> {
              if (ar.succeeded()) {
                testContext.fail();
              } else {
                testContext.assertTrue(ar.cause() instanceof NoSuchElementException);
                check1.set(true);
              }
            });
    Awaitility.await().until(() -> check1.get());
  }

}
