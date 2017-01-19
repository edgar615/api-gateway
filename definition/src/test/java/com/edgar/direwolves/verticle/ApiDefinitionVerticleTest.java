package com.edgar.direwolves.verticle;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.Endpoint;
import com.edgar.direwolves.core.definition.HttpEndpoint;
import com.edgar.util.base.Randoms;
import com.edgar.util.exception.DefaultErrorCode;
import com.google.common.collect.Lists;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.ReplyException;
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

import java.util.List;

/**
 * Created by Edgar on 2016/4/11.
 *
 * @author Edgar  Date 2016/4/11
 */
@RunWith(VertxUnitRunner.class)
public class ApiDefinitionVerticleTest {

  ApiDefinitionRegistry registry;

  Vertx vertx;

  @Before
  public void setUp(TestContext testContext) {
    registry = ApiDefinitionRegistry.create();
    HttpEndpoint httpEndpoint = Endpoint
        .createHttp("get_device", HttpMethod.GET, "devices/", "device");
    ApiDefinition apiDefinition = ApiDefinition
        .create("get_device", HttpMethod.GET, "device/", Lists.newArrayList(httpEndpoint));
    registry.add(apiDefinition);

    apiDefinition = ApiDefinition
        .create("get_device2", HttpMethod.GET, "device/", Lists.newArrayList(httpEndpoint));
    registry.add(apiDefinition);

    vertx = Vertx.vertx();
    vertx.deployVerticle(ApiDefinitionVerticle.class.getName(), testContext.asyncAssertSuccess());
  }

  @After
  public void clear() {
    registry.remove(null);
  }

  @Test
  public void testGetApiEventbus(TestContext testContext) {
    Async async = testContext.async();
    vertx.eventBus().<JsonObject>send("direwolves.eb.api.get",
        new JsonObject().put("name", "get_device"), ar -> {
          if (ar.succeeded()) {
            System.out.println(ar.result().body());
            async.complete();
          } else {
            ar.cause().printStackTrace();
            testContext.fail();
          }
        });
  }

  @Test
  public void testGetUndefinedApiEventbus(TestContext testContext) {
    Async async = testContext.async();
    vertx.eventBus().<JsonObject>send("direwolves.eb.api.get",
        new JsonObject().put("name", Randoms.randomAlphabet(10)), ar-> {
          if (ar.succeeded()) {
            testContext.fail();
          } else {
            testContext.assertTrue(ar.cause() instanceof ReplyException);
            ReplyException ex = (ReplyException) ar.cause();
            testContext.assertEquals(DefaultErrorCode.RESOURCE_NOT_FOUND.getNumber(), ex.failureCode());
            async.complete();
          }
        });
  }

}
