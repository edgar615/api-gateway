package com.edgar.direwolves.cmd;

import com.edgar.direwolves.core.cmd.ApiCmd;
import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.verticle.ApiDefinitionRegistry;
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

/**
 * Created by Edgar on 2017/1/19.
 *
 * @author Edgar  Date 2017/1/19
 */
@RunWith(VertxUnitRunner.class)
public class AddApiCmdTest {

  ApiDefinitionRegistry registry = ApiDefinitionRegistry.create();

  ApiCmd cmd;

  @Before
  public void setUp() {
    cmd = new AddApiCmdFactory().create(Vertx.vertx(), new JsonObject());
  }

  @After
  public void tearDown() {
    registry.remove("*");
  }

  @Test
  public void testAddApiSuccess(TestContext testContext) {
    Assert.assertEquals(0, registry.filter("*").size());
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

    Async async = testContext.async();
    cmd.handle(jsonObject)
            .setHandler(ar -> {
              if (ar.succeeded()) {
                testContext.assertEquals(1, ar.result().getInteger("result"));
                testContext.assertEquals(1, registry.filter("add_device").size());
                testContext.assertEquals(1, registry.filter("add_*").size());
                testContext.assertEquals(1, registry.filter("*_device").size());
                testContext.assertEquals(1, registry.filter("*").size());

                ApiDefinition apiDefinition = registry.filter("add_device").get(0);
                testContext.assertEquals(0, apiDefinition.plugins().size());
                async.complete();
              } else {
                testContext.fail();
              }
            });
  }

  @Test
  public void testAddApiFailed(TestContext testContext) {
    Assert.assertEquals(0, registry.filter("*").size());
    JsonObject jsonObject = new JsonObject()
            .put("name", "add_device")
            .put("path", "/devices");
    JsonArray endpoints = new JsonArray()
            .add(new JsonObject().put("type", "http")
                         .put("name", "add_device")
                         .put("method", "POST")
                         .put("path", "/devices"));
    jsonObject.put("endpoints", endpoints);

    Async async = testContext.async();
    cmd.handle(jsonObject)
            .setHandler(ar -> {
              if (ar.succeeded()) {
                testContext.fail();
              } else {
                Assert.assertEquals(0, registry.filter("*").size());
                async.complete();
              }
            });
  }

  @Test
  public void testAddApiWithPlugin(TestContext testContext) {
    Assert.assertEquals(0, registry.filter("*").size());
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

    Async async = testContext.async();
    cmd.handle(jsonObject)
            .setHandler(ar -> {
              if (ar.succeeded()) {
                testContext.assertEquals(1, ar.result().getInteger("result"));
                testContext.assertEquals(1, registry.filter("add_device").size());
                testContext.assertEquals(1, registry.filter("add_*").size());
                testContext.assertEquals(1, registry.filter("*_device").size());
                testContext.assertEquals(1, registry.filter("*").size());

                ApiDefinition apiDefinition = registry.filter("add_device").get(0);
                testContext.assertEquals(1, apiDefinition.plugins().size());

                async.complete();
              } else {
                testContext.fail();
              }
            });
  }

}
