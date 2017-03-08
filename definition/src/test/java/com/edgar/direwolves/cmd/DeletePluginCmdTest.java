package com.edgar.direwolves.cmd;

import com.google.common.collect.Lists;

import com.edgar.direwolves.core.cmd.ApiCmd;
import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.Endpoint;
import com.edgar.direwolves.core.definition.HttpEndpoint;
import com.edgar.direwolves.plugin.authorization.AuthorisePlugin;
import com.edgar.direwolves.verticle.ApiDefinitionRegistry;
import com.edgar.util.validation.ValidationException;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
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
public class DeletePluginCmdTest {

  ApiDefinitionRegistry registry = ApiDefinitionRegistry.create();

  ApiCmd cmd;

  ApiDefinition definition;

  @Before
  public void setUp() {
    HttpEndpoint httpEndpoint =
            Endpoint.http("get_device", HttpMethod.GET, "devices/", "device");

    definition = ApiDefinition
            .create("get_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));

    definition.addPlugin(AuthorisePlugin.create("device:get"));

    registry.add(definition);

    cmd = new ApiPluginCmdFactory().create(Vertx.vertx(), new JsonObject());
  }

  @After
  public void tearDown() {
    registry.remove(null);
  }

  @Test
  public void testMissNameShouldThrowValidationException(TestContext testContext) {
    ApiDefinition apiDefinition = registry.filter("get_device").get(0);
    Assert.assertEquals(1, apiDefinition.plugins().size());

    JsonObject jsonObject = new JsonObject()
            .put("name", "get_device")
            .put("subcmd", "delete");

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
  public void testDeletePlugin(TestContext testContext) {
    ApiDefinition apiDefinition = registry.filter("get_device").get(0);
    Assert.assertEquals(1, apiDefinition.plugins().size());

    JsonObject jsonObject = new JsonObject()
            .put("name", "get_device")
            .put("subcmd", "delete")
            .put("plugin", "AuthorisePlugin");

    Async async = testContext.async();
    cmd.handle(jsonObject)
            .setHandler(ar -> {
              if (ar.succeeded()) {
                ApiDefinition apiDefinition2 = registry.filter("get_device").get(0);
                Assert.assertEquals(0, apiDefinition2.plugins().size());
                async.complete();
              } else {
                testContext.fail();
              }
            });
  }
}
