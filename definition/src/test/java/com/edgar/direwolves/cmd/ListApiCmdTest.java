package com.edgar.direwolves.cmd;

import com.edgar.direvolves.plugin.authentication.AuthenticationPlugin;
import com.edgar.direwolves.core.cmd.ApiCmd;
import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.verticle.ApiDefinitionRegistry;
import com.edgar.util.base.Randoms;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.validation.ValidationException;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * Created by Edgar on 2017/1/19.
 *
 * @author Edgar  Date 2017/1/19
 */
@RunWith(VertxUnitRunner.class)
public class ListApiCmdTest {

  ApiDefinitionRegistry registry = ApiDefinitionRegistry.create();

  ApiCmd cmd;

  @Before
  public void setUp() {
    cmd = new ListApiCmdFactory().create(Vertx.vertx(), new JsonObject());

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

    registry.add(ApiDefinition.fromJson(jsonObject));

    jsonObject = new JsonObject()
        .put("name", "update_device")
        .put("method", "PUT")
        .put("path", "/devices");
    jsonObject.put("endpoints", endpoints);
    registry.add(ApiDefinition.fromJson(jsonObject));
  }

  @After
  public void tearDown() {
    registry.remove(null);
  }

  @Test
  public void testListAll(TestContext testContext) {
    Assert.assertEquals(2, registry.filter(null).size());
    JsonObject jsonObject = new JsonObject();

    Async async = testContext.async();
    cmd.handle(jsonObject)
            .setHandler(ar -> {
              if (ar.succeeded()) {
                JsonArray jsonArray = ar.result().getJsonArray("result");

                testContext.assertEquals(2, jsonArray.size());
                ApiDefinition apiDefinition = ApiDefinition.fromJson(jsonArray.getJsonObject(0));
                testContext.assertEquals("add_device", apiDefinition.name());
                apiDefinition = ApiDefinition.fromJson(jsonArray.getJsonObject(1));
                testContext.assertEquals("update_device", apiDefinition.name());
                async.complete();
              } else {
                testContext.fail();
              }
            });
  }

  @Test
  public void testListByFullname(TestContext testContext) {
    Assert.assertEquals(2, registry.filter(null).size());
    JsonObject jsonObject = new JsonObject()
        .put("name", "update_device");

    Async async = testContext.async();
    cmd.handle(jsonObject)
        .setHandler(ar -> {
          if (ar.succeeded()) {
            JsonArray jsonArray = ar.result().getJsonArray("result");

            testContext.assertEquals(1, jsonArray.size());
            ApiDefinition apiDefinition = ApiDefinition.fromJson(jsonArray.getJsonObject(0));
            testContext.assertEquals("update_device", apiDefinition.name());
            async.complete();
          } else {
            testContext.fail();
          }
        });
  }

  @Test
  public void testListByWildcard(TestContext testContext) {
    Assert.assertEquals(2, registry.filter(null).size());
    JsonObject jsonObject = new JsonObject()
        .put("name", "*device");

    Async async = testContext.async();
    cmd.handle(jsonObject)
        .setHandler(ar -> {
          if (ar.succeeded()) {
            JsonArray jsonArray = ar.result().getJsonArray("result");

            testContext.assertEquals(2, jsonArray.size());
            ApiDefinition apiDefinition = ApiDefinition.fromJson(jsonArray.getJsonObject(0));
            testContext.assertEquals("add_device", apiDefinition.name());
            apiDefinition = ApiDefinition.fromJson(jsonArray.getJsonObject(1));
            testContext.assertEquals("update_device", apiDefinition.name());
            async.complete();
          } else {
            testContext.fail();
          }
        });
  }

  @Test
  public void testListByUndefinedWildcard(TestContext testContext) {
    Assert.assertEquals(2, registry.filter(null).size());
    JsonObject jsonObject = new JsonObject()
        .put("name", "*zdferec");

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
    Assert.assertEquals(2, registry.filter(null).size());
    JsonObject jsonObject = new JsonObject()
        .put("name", "*")
        .put("start", 1);

    Async async = testContext.async();
    cmd.handle(jsonObject)
        .setHandler(ar -> {
          if (ar.succeeded()) {
            JsonArray jsonArray = ar.result().getJsonArray("result");

            testContext.assertEquals(1, jsonArray.size());
            ApiDefinition apiDefinition = ApiDefinition.fromJson(jsonArray.getJsonObject(0));
            testContext.assertEquals("update_device", apiDefinition.name());
            async.complete();
          } else {
            testContext.fail();
          }
        });
  }

  @Test
  public void testListByStartOut(TestContext testContext) {
    Assert.assertEquals(2, registry.filter(null).size());
    JsonObject jsonObject = new JsonObject()
        .put("start", 2);

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
    Assert.assertEquals(2, registry.filter(null).size());
    JsonObject jsonObject = new JsonObject()
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

  @Test
  public void testListByLimitZero(TestContext testContext) {
    Assert.assertEquals(2, registry.filter(null).size());
    JsonObject jsonObject = new JsonObject()
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
    Assert.assertEquals(2, registry.filter(null).size());
    JsonObject jsonObject = new JsonObject()
        .put("start", 1)
        .put("limit", 1);

    Async async = testContext.async();
    cmd.handle(jsonObject)
        .setHandler(ar -> {
          if (ar.succeeded()) {
            JsonArray jsonArray = ar.result().getJsonArray("result");

            testContext.assertEquals(1, jsonArray.size());
            ApiDefinition apiDefinition = ApiDefinition.fromJson(jsonArray.getJsonObject(0));
            testContext.assertEquals("update_device", apiDefinition.name());
            async.complete();
          } else {
            testContext.fail();
          }
        });
  }

}
