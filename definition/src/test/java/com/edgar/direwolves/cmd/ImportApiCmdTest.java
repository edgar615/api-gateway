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
public class ImportApiCmdTest {

  ApiDefinitionRegistry registry = ApiDefinitionRegistry.create();

  ApiCmd cmd;

  @Before
  public void setUp() {
    cmd = new ImportApiCmdFactory().create(Vertx.vertx(), new JsonObject());
  }

  @After
  public void tearDown() {
    registry.remove("*");
  }

  @Test
  public void testImportDirSuccess(TestContext testContext) {

    JsonObject jsonObject = new JsonObject()
        .put("path", "/home/edgar/dev/workspace/direwolves/definition/src/test/resources/api");
    Async async = testContext.async();
    cmd.handle(jsonObject)
            .setHandler(ar -> {
              if (ar.succeeded()) {
                testContext.assertEquals(1, ar.result().getInteger("result"));
                testContext.assertEquals(1, registry.filter("add_device").size());
                testContext.assertEquals(1, registry.filter("add_*").size());
                testContext.assertEquals(2, registry.filter("*_device").size());
                testContext.assertEquals(2, registry.filter("*").size());

                ApiDefinition apiDefinition = registry.filter("add_device").get(0);
                testContext.assertEquals(0, apiDefinition.plugins().size());
                async.complete();
              } else {
                testContext.fail();
              }
            });
  }

  @Test
  public void testImportFileSuccess(TestContext testContext) {

    JsonObject jsonObject = new JsonObject()
        .put("path", "/home/edgar/dev/workspace/direwolves/definition/src/test/resources/api/device_add.json");
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
  public void testInvalidJsonShouldNotAddAnyApi(TestContext testContext) {

    JsonObject jsonObject = new JsonObject()
        .put("path", "/home/edgar/dev/workspace/direwolves/definition/src/test/resources/invalid");
    Async async = testContext.async();
    cmd.handle(jsonObject)
        .setHandler(ar -> {
          if (ar.succeeded()) {
            testContext.fail();
          } else {
            testContext.assertEquals(0, registry.filter(null).size());
            async.complete();
          }
        });
  }

}
