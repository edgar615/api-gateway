package com.edgar.direwolves.cmd;

import com.edgar.direwolves.core.cmd.ApiCmd;
import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.ApiDiscovery;
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

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Edgar on 2017/1/19.
 *
 * @author Edgar  Date 2017/1/19
 */
@RunWith(VertxUnitRunner.class)
public class ListApiCmdTest {


  ApiDiscovery discovery;

  ApiCmd cmd;

  String namespace;

  Vertx vertx;

  @Before
  public void setUp() {
    namespace = UUID.randomUUID().toString();
    vertx = Vertx.vertx();
    discovery = ApiDiscovery.create(vertx, namespace);
    cmd = new ListApiCmdFactory().create(vertx, new JsonObject());

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
  public void testListAll(TestContext testContext) {
    JsonObject jsonObject = new JsonObject()
            .put("namespace", namespace);

    Async async = testContext.async();
    cmd.handle(jsonObject)
            .setHandler(ar -> {
              if (ar.succeeded()) {
                JsonArray jsonArray = ar.result().getJsonArray("result");

                testContext.assertEquals(2, jsonArray.size());
                ApiDefinition apiDefinition = ApiDefinition.fromJson(jsonArray.getJsonObject(0));
                testContext.assertEquals("get_device", apiDefinition.name());
                apiDefinition = ApiDefinition.fromJson(jsonArray.getJsonObject(1));
                testContext.assertEquals("add_device", apiDefinition.name());
                async.complete();
              } else {
                ar.cause().printStackTrace();
                testContext.fail();
              }
            });

  }

  @Test
  public void testListByFullname(TestContext testContext) {
    JsonObject jsonObject = new JsonObject()
            .put("namespace", namespace)
            .put("name", "get_device");

    Async async = testContext.async();
    cmd.handle(jsonObject)
            .setHandler(ar -> {
              if (ar.succeeded()) {
                JsonArray jsonArray = ar.result().getJsonArray("result");

                testContext.assertEquals(1, jsonArray.size());
                ApiDefinition apiDefinition = ApiDefinition.fromJson(jsonArray.getJsonObject(0));
                testContext.assertEquals("get_device", apiDefinition.name());
                async.complete();
              } else {
                testContext.fail();
              }
            });
  }

  @Test
  public void testListByWildcard(TestContext testContext) {
    JsonObject jsonObject = new JsonObject()
            .put("namespace", namespace)
            .put("name", "*device");

    Async async = testContext.async();
    cmd.handle(jsonObject)
            .setHandler(ar -> {
              if (ar.succeeded()) {
                JsonArray jsonArray = ar.result().getJsonArray("result");

                testContext.assertEquals(2, jsonArray.size());
                ApiDefinition apiDefinition = ApiDefinition.fromJson(jsonArray.getJsonObject(0));
                testContext.assertEquals("get_device", apiDefinition.name());
                apiDefinition = ApiDefinition.fromJson(jsonArray.getJsonObject(1));
                testContext.assertEquals("add_device", apiDefinition.name());
                async.complete();
              } else {
                testContext.fail();
              }
            });
  }

  @Test
  public void testListByUndefinedWildcard(TestContext testContext) {
    JsonObject jsonObject = new JsonObject()
            .put("namespace", namespace)
            .put("name", "*rererere");

    Async async = testContext.async();
    cmd.handle(jsonObject)
            .setHandler(ar -> {
              if (ar.succeeded()) {
                JsonArray jsonArray = ar.result().getJsonArray("result");

                testContext.assertEquals(0, jsonArray.size());
                async.complete();
              } else {
                testContext.fail();
              }
            });
  }

  @Test
  public void testListByStart(TestContext testContext) {
    JsonObject jsonObject = new JsonObject()
            .put("namespace", namespace)
            .put("name", "*")
            .put("start", 1);

    Async async = testContext.async();
    cmd.handle(jsonObject)
            .setHandler(ar -> {
              if (ar.succeeded()) {
                JsonArray jsonArray = ar.result().getJsonArray("result");

                testContext.assertEquals(1, jsonArray.size());
                ApiDefinition apiDefinition = ApiDefinition.fromJson(jsonArray.getJsonObject(0));
                testContext.assertEquals("add_device", apiDefinition.name());
                async.complete();
              } else {
                testContext.fail();
              }
            });
  }

  @Test
  public void testListByStartOut(TestContext testContext) {
    JsonObject jsonObject = new JsonObject()
            .put("namespace", namespace)
            .put("start", 3);

    Async async = testContext.async();
    cmd.handle(jsonObject)
        .setHandler(ar -> {
          if (ar.succeeded()) {
            JsonArray jsonArray = ar.result().getJsonArray("result");

            testContext.assertEquals(0, jsonArray.size());
            async.complete();
          } else {
            testContext.fail();
          }
        });
  }

  @Test
  public void testListByLimit(TestContext testContext) {
    JsonObject jsonObject = new JsonObject()
            .put("namespace", namespace)
            .put("limit", 1);

    Async async = testContext.async();
    cmd.handle(jsonObject)
        .setHandler(ar -> {
          if (ar.succeeded()) {
            JsonArray jsonArray = ar.result().getJsonArray("result");

            testContext.assertEquals(1, jsonArray.size());
            ApiDefinition apiDefinition = ApiDefinition.fromJson(jsonArray.getJsonObject(0));
            testContext.assertEquals("get_device", apiDefinition.name());
            async.complete();
          } else {
            testContext.fail();
          }
        });
  }

  @Test
  public void testListByLimitZero(TestContext testContext) {
    JsonObject jsonObject = new JsonObject()
            .put("namespace", namespace)
            .put("limit", 0);

    Async async = testContext.async();
    cmd.handle(jsonObject)
        .setHandler(ar -> {
          if (ar.succeeded()) {
            JsonArray jsonArray = ar.result().getJsonArray("result");

            testContext.assertEquals(0, jsonArray.size());
            async.complete();
          } else {
            testContext.fail();
          }
        });
  }

  @Test
  public void testListByStartAndLimit(TestContext testContext) {
    JsonObject jsonObject = new JsonObject()
            .put("namespace", namespace)
        .put("start", 1)
        .put("limit", 1);

    Async async = testContext.async();
    cmd.handle(jsonObject)
        .setHandler(ar -> {
          if (ar.succeeded()) {
            JsonArray jsonArray = ar.result().getJsonArray("result");

            testContext.assertEquals(1, jsonArray.size());
            ApiDefinition apiDefinition = ApiDefinition.fromJson(jsonArray.getJsonObject(0));
            testContext.assertEquals("add_device", apiDefinition.name());
            async.complete();
          } else {
            testContext.fail();
          }
        });
  }

}
