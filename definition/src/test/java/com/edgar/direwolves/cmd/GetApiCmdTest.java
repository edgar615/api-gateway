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

/**
 * Created by Edgar on 2017/1/19.
 *
 * @author Edgar  Date 2017/1/19
 */
@RunWith(VertxUnitRunner.class)
public class GetApiCmdTest {

  ApiDefinitionRegistry registry = ApiDefinitionRegistry.create();

  ApiCmd cmd;

  @Before
  public void setUp() {
    cmd = new GetApiCmdFactory().create(Vertx.vertx(), new JsonObject());

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
  public void testGetApiByFullname(TestContext testContext) {
    Assert.assertEquals(1, registry.filter(null).size());
    JsonObject jsonObject = new JsonObject()
            .put("name", "add_device");

    Async async = testContext.async();
    cmd.handle(jsonObject)
            .setHandler(ar -> {
              if (ar.succeeded()) {
                ApiDefinition definition = ApiDefinition.fromJson(ar.result());
                testContext.assertEquals("add_device", definition.name());
                testContext.assertEquals(1, definition.plugins().size());
                testContext.assertNotNull(definition.plugin(
                        AuthenticationPlugin.class.getSimpleName()));
                async.complete();
              } else {
                testContext.fail();
              }
            });
  }

  @Test
  public void testGetApiByWildcard(TestContext testContext) {
    Assert.assertEquals(1, registry.filter(null).size());
    JsonObject jsonObject = new JsonObject()
            .put("name", "*_device");

    Async async = testContext.async();
    cmd.handle(jsonObject)
            .setHandler(ar -> {
              if (ar.succeeded()) {
                ApiDefinition definition = ApiDefinition.fromJson(ar.result());
                testContext.assertEquals("add_device", definition.name());
                testContext.assertEquals(1, definition.plugins().size());
                testContext.assertNotNull(definition.plugin(
                        AuthenticationPlugin.class.getSimpleName()));
                async.complete();
              } else {
                testContext.fail();
              }
            });
  }

  @Test
  public void testGetApiByUndefinedNameShouldSuccess(TestContext testContext) {
    Assert.assertEquals(1, registry.filter(null).size());
    JsonObject jsonObject = new JsonObject()
            .put("name", Randoms.randomAlphabetAndNum(10));

    Async async = testContext.async();
    cmd.handle(jsonObject)
            .setHandler(ar -> {
              if (ar.succeeded()) {
                testContext.fail();
              } else {
                testContext.assertTrue(ar.cause() instanceof SystemException);
                SystemException ex = (SystemException) ar.cause();
                testContext.assertEquals(DefaultErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
                async.complete();
              }
            });
  }

  @Test
  public void testGetApiByUndefinedWildcardShouldSuccess(TestContext testContext) {
    Assert.assertEquals(1, registry.filter(null).size());
    JsonObject jsonObject = new JsonObject()
            .put("name", "add.*");

    Async async = testContext.async();
    cmd.handle(jsonObject)
            .setHandler(ar -> {
              if (ar.succeeded()) {
                testContext.fail();
              } else {
                testContext.assertTrue(ar.cause() instanceof SystemException);
                SystemException ex = (SystemException) ar.cause();
                testContext.assertEquals(DefaultErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
                async.complete();
              }
            });
  }

}
