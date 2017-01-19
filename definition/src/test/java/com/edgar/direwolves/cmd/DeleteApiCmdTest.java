package com.edgar.direwolves.cmd;

import com.edgar.direwolves.core.cmd.ApiCmd;
import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.verticle.ApiDefinitionRegistry;
import com.edgar.util.base.Randoms;
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

/**
 * Created by Edgar on 2017/1/19.
 *
 * @author Edgar  Date 2017/1/19
 */
@RunWith(VertxUnitRunner.class)
public class DeleteApiCmdTest {

  ApiDefinitionRegistry registry = ApiDefinitionRegistry.create();

  ApiCmd cmd;

  @Before
  public void setUp() {
    cmd = new DeleteApiCmdFactory().create(Vertx.vertx(), new JsonObject());

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

    registry.add(ApiDefinition.fromJson(jsonObject));
  }

  @After
  public void tearDown() {
    registry.remove(null);
  }

  @Test
  public void testMissNameShouldThrowValidationException(TestContext testContext) {
    Assert.assertEquals(1, registry.filter(null).size());
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
    Assert.assertEquals(1, registry.filter(null).size());
    JsonObject jsonObject = new JsonObject()
            .put("name", "add_device");

    Async async = testContext.async();
    cmd.handle(jsonObject)
            .setHandler(ar -> {
              if (ar.succeeded()) {
                Assert.assertEquals(0, registry.filter(null).size());
                async.complete();
              } else {
                testContext.fail();
              }
            });
  }

  @Test
  public void testDeleteApiByUndefinedNameShouldSuccess(TestContext testContext) {
    Assert.assertEquals(1, registry.filter(null).size());
    JsonObject jsonObject = new JsonObject()
            .put("name", Randoms.randomAlphabetAndNum(10));

    Async async = testContext.async();
    cmd.handle(jsonObject)
            .setHandler(ar -> {
              if (ar.succeeded()) {
                Assert.assertEquals(1, registry.filter(null).size());
                async.complete();
              } else {
                testContext.fail();
              }
            });
  }

  @Test
  public void testDeleteApiByWildcard(TestContext testContext) {
    Assert.assertEquals(1, registry.filter(null).size());
    JsonObject jsonObject = new JsonObject()
            .put("name", "add*");

    Async async = testContext.async();
    cmd.handle(jsonObject)
            .setHandler(ar -> {
              if (ar.succeeded()) {
                Assert.assertEquals(0, registry.filter(null).size());
                async.complete();
              } else {
                testContext.fail();
              }
            });
  }

}
